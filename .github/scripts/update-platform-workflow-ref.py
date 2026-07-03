#!/usr/bin/env python3
"""Update or verify platform reusable workflow refs in this repository."""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
PLATFORM_WORKFLOWS = ROOT / ".platform" / "workflows.yml"
GITHUB_WORKFLOWS = ROOT / ".github" / "workflows"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Update .platform/workflows.yml and literal platform workflow refs."
    )
    parser.add_argument(
        "ref",
        nargs="?",
        help="Target platform workflow ref. Required unless --check uses the configured ref.",
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="Verify refs without changing files. Optionally pass the expected ref.",
    )
    return parser.parse_args()


def read_platform_config() -> tuple[str, str, str]:
    text = PLATFORM_WORKFLOWS.read_text(encoding="utf-8")
    repository_match = re.search(r"(?m)^\s*repository:\s*([^\s#]+)\s*$", text)
    ref_match = re.search(r"(?m)^(\s*ref:\s*)([^\s#]+)(\s*(?:#.*)?)$", text)
    if repository_match is None:
        raise SystemExit(f"Missing platform_workflows.repository in {PLATFORM_WORKFLOWS}")
    if ref_match is None:
        raise SystemExit(f"Missing platform_workflows.ref in {PLATFORM_WORKFLOWS}")
    return text, repository_match.group(1), ref_match.group(2)


def workflow_files() -> list[Path]:
    files = list(GITHUB_WORKFLOWS.glob("*.yml")) + list(GITHUB_WORKFLOWS.glob("*.yaml"))
    return sorted(files)


def workflow_ref_pattern(repository: str) -> re.Pattern[str]:
    escaped_repository = re.escape(repository)
    return re.compile(
        rf"(?P<prefix>uses:\s*{escaped_repository}/\.github/workflows/[^@\s]+@)"
        r"(?P<ref>[A-Za-z0-9._/-]+)"
    )


def update_platform_config(text: str, expected_ref: str) -> str:
    def replace(match: re.Match[str]) -> str:
        return f"{match.group(1)}{expected_ref}{match.group(3)}"

    updated, count = re.subn(
        r"(?m)^(\s*ref:\s*)([^\s#]+)(\s*(?:#.*)?)$",
        replace,
        text,
        count=1,
    )
    if count != 1:
        raise SystemExit(f"Could not update platform_workflows.ref in {PLATFORM_WORKFLOWS}")
    return updated


def collect_workflow_refs(repository: str) -> list[tuple[Path, str]]:
    pattern = workflow_ref_pattern(repository)
    refs: list[tuple[Path, str]] = []
    for path in workflow_files():
        text = path.read_text(encoding="utf-8")
        for match in pattern.finditer(text):
            refs.append((path, match.group("ref")))
    return refs


def check_refs(repository: str, configured_ref: str, expected_ref: str) -> int:
    failures: list[str] = []
    if configured_ref != expected_ref:
        failures.append(
            f"{PLATFORM_WORKFLOWS.relative_to(ROOT)} has ref {configured_ref}, expected {expected_ref}"
        )

    refs = collect_workflow_refs(repository)
    if not refs:
        failures.append(f"No reusable workflow refs found for {repository}")

    for path, actual_ref in refs:
        if actual_ref != expected_ref:
            failures.append(
                f"{path.relative_to(ROOT)} uses {repository}@{actual_ref}, expected @{expected_ref}"
            )

    if failures:
        for failure in failures:
            print(failure, file=sys.stderr)
        return 1

    print(f"All {repository} workflow refs use {expected_ref}")
    return 0


def update_refs(repository: str, platform_text: str, expected_ref: str) -> int:
    PLATFORM_WORKFLOWS.write_text(
        update_platform_config(platform_text, expected_ref),
        encoding="utf-8",
    )

    pattern = workflow_ref_pattern(repository)
    changed_files: list[Path] = [PLATFORM_WORKFLOWS]
    for path in workflow_files():
        text = path.read_text(encoding="utf-8")

        def replace(match: re.Match[str]) -> str:
            return f"{match.group('prefix')}{expected_ref}"

        updated = pattern.sub(replace, text)
        if updated != text:
            path.write_text(updated, encoding="utf-8")
            changed_files.append(path)

    for path in changed_files:
        print(path.relative_to(ROOT))
    return 0


def main() -> int:
    args = parse_args()
    platform_text, repository, configured_ref = read_platform_config()
    expected_ref = args.ref or configured_ref
    if not expected_ref:
        print("A target ref is required", file=sys.stderr)
        return 2

    if args.check:
        return check_refs(repository, configured_ref, expected_ref)

    if args.ref is None:
        print("A target ref is required when updating", file=sys.stderr)
        return 2

    return update_refs(repository, platform_text, expected_ref)


if __name__ == "__main__":
    raise SystemExit(main())
