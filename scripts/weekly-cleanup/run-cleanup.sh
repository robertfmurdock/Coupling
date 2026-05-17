#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel)"
PLAN_FILE="${ROOT_DIR}/build/weekly-cleanup/plan.env"
PROMPT_FILE="${CLEANUP_PROMPT_FILE:-${ROOT_DIR}/build/weekly-cleanup/prompt.md}"
BEDROCK_REGION="${AWS_REGION:-us-east-1}"
MAX_TURNS="${WEEKLY_CLEANUP_MAX_TURNS:-40}"

if [[ -n "${BEDROCK_INFERENCE_PROFILE_ID:-}" ]]; then
  BEDROCK_MODEL="${BEDROCK_INFERENCE_PROFILE_ID}"
elif [[ -n "${BEDROCK_MODEL_ID:-}" ]]; then
  BEDROCK_MODEL="${BEDROCK_MODEL_ID}"
else
  cat >&2 <<EOF
Either BEDROCK_INFERENCE_PROFILE_ID or BEDROCK_MODEL_ID is required.
For Anthropic Claude Sonnet 4, use an inference profile ID/ARN because
on-demand throughput with direct model IDs is not supported.
EOF
  exit 1
fi

if [[ ! -f "${PROMPT_FILE}" ]]; then
  echo "Prompt file not found: ${PROMPT_FILE}" >&2
  exit 1
fi

if [[ ! -f "${PLAN_FILE}" ]]; then
  echo "Plan file not found: ${PLAN_FILE}" >&2
  exit 1
fi

STREAM=$(mktemp)
trap 'rm -f "$STREAM"' EXIT

CLAUDE_CODE_USE_BEDROCK=1 \
  AWS_REGION="${BEDROCK_REGION}" \
  ANTHROPIC_MODEL="${BEDROCK_MODEL}" \
  claude --dangerously-skip-permissions -p --verbose --output-format stream-json --max-turns "${MAX_TURNS}" "$(cat "${PROMPT_FILE}")" \
  | tee "$STREAM" \
  | jq -r 'select(.type == "assistant") | .message.content[] | select(.type == "text") | .text'

subtype=$(jq -r 'select(.type == "result") | .subtype' "$STREAM")
result_error=$(jq -r 'select(.type == "result") | .error // ""' "$STREAM")

if [[ "$subtype" == "error_max_turns" ]]; then
  echo "Agent hit max turns — partial work may exist" >&2
  exit 0
elif [[ "$subtype" == "success" ]]; then
  exit 0
else
  echo "Agent failed: subtype='${subtype}'" >&2
  if [[ -n "$result_error" ]]; then
    echo "Error details: ${result_error}" >&2
  fi
  echo "--- Last 30 stream events for debugging ---" >&2
  tail -30 "$STREAM" >&2
  exit 1
fi
