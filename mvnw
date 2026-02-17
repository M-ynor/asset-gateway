#!/bin/sh
set -e
MAVEN_HOME="${MAVEN_HOME:-$HOME/.m2/wrapper/dists}"
if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi
echo "Maven not found. Install Maven or use Docker: docker compose up"
exit 1
