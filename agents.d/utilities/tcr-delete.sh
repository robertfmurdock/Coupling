#!/usr/bin/env bash
# TCR-style delete: remove FILE, run full build, revert + record verdict automatically.
# Usage: ./scripts/weekly-cleanup/tcr-delete.sh <file> [reason]
set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 <file> [reason]" >&2
    exit 1
fi

FILE="$1"
REASON="${2:-}"
DATE="${CLEANUP_DATE:-$(date +%Y-%m-%d)}"
FOCUS="${CLEANUP_FOCUS:-unknown}"
RUN_ID="${CLEANUP_RUN_ID:-}"
ROOT_DIR="$(git rev-parse --show-toplevel)"
HISTORY="${ROOT_DIR}/.github/weekly-cleanup/cleanup-history.md"

if [[ ! -f "$FILE" ]]; then
    echo "File not found: $FILE" >&2
    exit 1
fi

BASENAME="$(basename "$FILE")"

rm "$FILE"

if ./gradlew check -q --console=plain 2>&1 | tail -100; then
    VERDICT="deleted"
    NOTE="${REASON:-cross-module build clean}"
else
    git checkout -- "$FILE"
    VERDICT="verified-in-use"
    NOTE="${REASON:-full build failed after deletion}"
fi

HEADER="## ${DATE}${RUN_ID:+ (run-${RUN_ID})} — ${FOCUS}"
grep -qF "$HEADER" "$HISTORY" || printf "\n%s\n" "$HEADER" >> "$HISTORY"

printf -- "- %s: %s — %s\n" "$BASENAME" "$VERDICT" "$NOTE" >> "$HISTORY"
echo "TCR result: $VERDICT — $NOTE"
