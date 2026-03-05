#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: scripts/graphql-ref-check.sh <pattern> [pattern...]"
  exit 2
fi

targets=(
  "server/src/jsMain/resources/schema.graphqls"
  "server/src/jsMain/kotlin"
  "sdk/src/commonMain/graphql"
  "sdk/src/commonMain/kotlin"
  "client"
)

for pattern in "$@"; do
  echo "== ${pattern} =="
  rg -n --no-heading "${pattern}" "${targets[@]}" || true
  echo
done
