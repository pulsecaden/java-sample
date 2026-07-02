#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_PROPS="$ROOT_DIR/.mvn/wrapper/maven-wrapper.properties"
DISTRIBUTION_URL=$(sed -n 's/^distributionUrl=//p' "$WRAPPER_PROPS")
MAVEN_VERSION=$(printf '%s\n' "$DISTRIBUTION_URL" | sed -n 's|.*/apache-maven-\([0-9][^/]*\)-bin\.tar\.gz|\1|p')

if [ -z "$MAVEN_VERSION" ]; then
  echo "Cannot determine Maven version from $WRAPPER_PROPS" >&2
  exit 1
fi

DIST_DIR="$ROOT_DIR/.mvn/wrapper/dists/apache-maven-$MAVEN_VERSION"
MAVEN_HOME="$DIST_DIR/apache-maven-$MAVEN_VERSION"
ARCHIVE="$DIST_DIR/apache-maven-$MAVEN_VERSION-bin.tar.gz"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$DIST_DIR"
  if [ ! -f "$ARCHIVE" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "$DISTRIBUTION_URL" -o "$ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "$DISTRIBUTION_URL" -O "$ARCHIVE"
    else
      echo "curl or wget is required to download Maven $MAVEN_VERSION" >&2
      exit 1
    fi
  fi
  tar -xzf "$ARCHIVE" -C "$DIST_DIR"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
