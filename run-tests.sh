#!/bin/sh
set -e
cd "$(dirname "$0")"
docker run --rm -v "$(pwd)":/app -w /app maven:3.9-eclipse-temurin-21-alpine mvn test
