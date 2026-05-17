#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel)"
PLAN_FILE="${ROOT_DIR}/build/weekly-cleanup/plan.env"
PROMPT_FILE="${CLEANUP_PROMPT_FILE:-${ROOT_DIR}/build/weekly-cleanup/prompt.md}"
BEDROCK_REGION="${AWS_REGION:-us-east-1}"

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

CLAUDE_CODE_USE_BEDROCK=1 \
  AWS_REGION="${BEDROCK_REGION}" \
  ANTHROPIC_MODEL="${BEDROCK_MODEL}" \
  claude -p "$(cat "${PROMPT_FILE}")"
