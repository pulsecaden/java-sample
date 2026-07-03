#!/usr/bin/env bash
# Runs local Java sample CI gates with resumable top-level checkpoints.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT_DIR"

RESUME=0
RESET_RESUME=0
STATE_DIR="${CI_LOCAL_ALL_STATE_DIR:-${ROOT_DIR}/.tmp/ci-local-all}"

if [[ -z "${STATE_DIR}" || "${STATE_DIR}" == "/" ]]; then
  echo "Refusing unsafe CI_LOCAL_ALL_STATE_DIR=${STATE_DIR:-<empty>}." >&2
  exit 2
fi

usage() {
  cat <<'USAGE'
Usage: .github/scripts/run-local-all.sh [--resume] [--reset-resume]

Options:
  --resume        Skip top-level CI stages that completed successfully in the last interrupted or failed run.
  --reset-resume  Clear saved resume checkpoints before running.
  --help          Show this help.

Equivalent npm usage:
  npm run ci:local:all
  npm run ci:local:resume
USAGE
}

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --resume)
      RESUME=1
      ;;
    --reset-resume)
      RESET_RESUME=1
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
  shift
done

if [[ "${RESET_RESUME}" == "1" || "${RESUME}" != "1" ]]; then
  rm -rf "${STATE_DIR}"
fi
mkdir -p "${STATE_DIR}"

resume_file() {
  local id="$1"
  printf '%s/%s.done\n' "${STATE_DIR}" "${id}"
}

run_ci_step() {
  local id="$1"
  local label="$2"
  shift 2
  local checkpoint
  local status=0
  checkpoint="$(resume_file "${id}")"

  if [[ "${RESUME}" == "1" && -f "${checkpoint}" ]]; then
    echo "Skipping ${label}; resume checkpoint exists at ${checkpoint}."
    return 0
  fi

  echo
  echo "::group::${label}"
  "$@" || status=$?
  echo "::endgroup::"

  if [[ "${status}" != "0" ]]; then
    rm -f "${checkpoint}"
    echo "Step failed [${label}] with exit code ${status}." >&2
    echo "Resume after fixing with: npm run ci:local:resume" >&2
    return "${status}"
  fi

  {
    printf 'label=%s\n' "${label}"
    printf 'completed_at=%s\n' "$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  } >"${checkpoint}"
}

require_command() {
  local command_name="$1"
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "Required command not found: ${command_name}" >&2
    return 127
  fi
}

java_major_version() {
  java -XshowSettings:properties -version 2>&1 \
    | awk -F= '/java.specification.version/ { gsub(/^[ \t]+|[ \t]+$/, "", $2); print $2; exit }'
}

check_java_21() {
  require_command java
  local version
  version="$(java_major_version)"
  if [[ "${version}" != "21" ]]; then
    echo "JDK 21 is required; current java.specification.version is ${version:-unknown}." >&2
    echo "Set JAVA_HOME to a JDK 21 install before running local CI." >&2
    return 1
  fi
  ./mvnw -version
}

workflow_lint() {
  require_command actionlint
  actionlint .github/workflows/ci.yml
}

maven_test() {
  ./mvnw clean test
}

maven_package() {
  ./mvnw package -DskipTests
}

sam_validate() {
  if ! command -v sam >/dev/null 2>&1; then
    echo "Skipping SAM validation; sam CLI is not installed."
    return 0
  fi
  sam validate --template micronaut-lambda/template.yaml --lint
  sam validate --template core-java-lambda/template.yaml --lint
}

run_ci_step "java_21" "JDK 21 check" check_java_21
run_ci_step "platform_refs" "Platform workflow ref guard" .github/scripts/update-platform-workflow-ref.py --check
run_ci_step "workflow_lint" "GitHub Actions workflow lint" workflow_lint
run_ci_step "maven_test" "Maven clean test" maven_test
run_ci_step "maven_package" "Maven package artifacts" maven_package
run_ci_step "sam_validate" "SAM template validation" sam_validate

rm -rf "${STATE_DIR}"
echo "Local CI command set completed."
