#!/bin/sh
set -eu
GRADLE_VERSION=8.9
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/caches/alazzi-gradle"
DIST="$CACHE_DIR/gradle-$GRADLE_VERSION"
if [ ! -x "$DIST/bin/gradle" ]; then
  mkdir -p "$CACHE_DIR"
  ZIP="$CACHE_DIR/gradle-$GRADLE_VERSION-bin.zip"
  if [ ! -f "$ZIP" ]; then curl -fL --retry 3 -o "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"; fi
  rm -rf "$DIST" "$CACHE_DIR/unpack"
  mkdir -p "$CACHE_DIR/unpack"
  unzip -q "$ZIP" -d "$CACHE_DIR/unpack"
  mv "$CACHE_DIR/unpack/gradle-$GRADLE_VERSION" "$DIST"
  rm -rf "$CACHE_DIR/unpack"
fi
exec "$DIST/bin/gradle" "$@"
