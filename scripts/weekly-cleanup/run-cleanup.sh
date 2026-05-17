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

STREAM_DIR="${ROOT_DIR}/build/weekly-cleanup"
mkdir -p "${STREAM_DIR}"
STREAM="${STREAM_DIR}/agent-stream.jsonl"
: > "$STREAM"

dump_stream_tail() {
  local n="${1:-30}"
  echo "--- Stream size: $(wc -l < "$STREAM") lines ---" >&2
  echo "--- Last ${n} stream events ---" >&2
  tail -${n} "$STREAM" >&2
}

# Disable errexit for the pipeline so we can capture per-stage exit codes.
set +e
CLAUDE_CODE_USE_BEDROCK=1 \
  AWS_REGION="${BEDROCK_REGION}" \
  ANTHROPIC_MODEL="${BEDROCK_MODEL}" \
  claude --dangerously-skip-permissions -p --verbose --output-format stream-json --max-turns "${MAX_TURNS}" "$(cat "${PROMPT_FILE}")" \
  | tee "$STREAM" \
  | jq -r 'select(.type == "assistant") | .message.content[]? | select(.type == "text") | .text'
pipeline_exits=("${PIPESTATUS[@]}")
set -e

claude_exit="${pipeline_exits[0]}"
jq_exit="${pipeline_exits[2]}"
echo "Pipeline exit codes — claude: ${claude_exit}, jq: ${jq_exit}" >&2

subtype=$(jq -r 'select(.type == "result") | .subtype' "$STREAM")
result_error=$(jq -r 'select(.type == "result") | .error // ""' "$STREAM")

echo "Agent result subtype: '${subtype:-<none>}'" >&2

if [[ "$subtype" == "error_max_turns" ]]; then
  echo "Agent hit max turns — partial work may exist" >&2
  exit 0
elif [[ "$subtype" == "success" ]]; then
  exit 0
elif [[ -z "$subtype" && "$claude_exit" -ne 0 ]]; then
  echo "claude process exited ${claude_exit} before producing a result event" >&2
  dump_stream_tail 30
  exit 1
else
  echo "Agent failed: subtype='${subtype}'" >&2
  if [[ -n "$result_error" ]]; then
    echo "Error details: ${result_error}" >&2
  fi
  dump_stream_tail 30
  exit 1
fi
