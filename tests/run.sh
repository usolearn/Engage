#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
out=$(mktemp -d)
trap 'rm -rf "$out"' EXIT
javac --release 17 -d "$out" app/src/main/java/org/spock/companion/CommandRules.java tests/CommandRulesTest.java
java -cp "$out" org.spock.companion.CommandRulesTest
