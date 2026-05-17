#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel)"
PLAN_FILE="${ROOT_DIR}/build/weekly-cleanup/plan.env"
PROMPT_FILE="${CLEANUP_PROMPT_FILE:-${ROOT_DIR}/build/weekly-cleanup/prompt.md}"
BEDROCK_MODEL_ID="${BEDROCK_MODEL_ID:-}"
BEDROCK_INFERENCE_PROFILE_ID="${BEDROCK_INFERENCE_PROFILE_ID:-}"
MAX_TURNS="${WEEKLY_CLEANUP_MAX_TURNS:-3}"
BEDROCK_REGION="${AWS_REGION:-us-east-1}"

if [[ -n "${BEDROCK_INFERENCE_PROFILE_ID}" ]]; then
  BEDROCK_MODEL_REF="${BEDROCK_INFERENCE_PROFILE_ID}"
elif [[ -n "${BEDROCK_MODEL_ID}" ]]; then
  BEDROCK_MODEL_REF="${BEDROCK_MODEL_ID}"
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

# shellcheck disable=SC1090
source "${PLAN_FILE}"

if [[ -z "${MODULE_TASK:-}" ]]; then
  echo "MODULE_TASK is missing from ${PLAN_FILE}" >&2
  exit 1
fi

BASE_PROMPT="$(cat "${PROMPT_FILE}")"
FEEDBACK=""
TURN=1

while [[ "${TURN}" -le "${MAX_TURNS}" ]]; do
  echo "Weekly cleanup turn ${TURN}/${MAX_TURNS}"
  RUN_DIR="${ROOT_DIR}/build/weekly-cleanup/turn-${TURN}"
  mkdir -p "${RUN_DIR}"

  USER_PROMPT_FILE="${RUN_DIR}/user-prompt.txt"
  RESPONSE_FILE="${RUN_DIR}/response.json"
  RAW_TEXT_FILE="${RUN_DIR}/response.txt"
  PATCH_FILE="${RUN_DIR}/proposal.patch"
  TEST_OUTPUT_FILE="${RUN_DIR}/module-check-output.txt"

  cat > "${USER_PROMPT_FILE}" <<EOF
${BASE_PROMPT}

Execution requirements:
- Return only one unified git diff in a fenced code block labeled diff.
- Keep changes within focus area: ${FOCUS}
- Keep changes small and architecture-aligned.
- Do not include explanations outside the diff block.
EOF

  if [[ -n "${FEEDBACK}" ]]; then
    cat >> "${USER_PROMPT_FILE}" <<EOF

Feedback from previous attempt:
${FEEDBACK}
EOF
  fi

  aws bedrock-runtime converse \
    --region "${BEDROCK_REGION}" \
    --model-id "${BEDROCK_MODEL_REF}" \
    --messages "$(jq -cn --rawfile p "${USER_PROMPT_FILE}" '[{role:"user",content:[{text:$p}]}]')" \
    --inference-config '{"maxTokens":4096,"temperature":0.1,"topP":0.9}' \
    > "${RESPONSE_FILE}"

  jq -r '.output.message.content[] | select(has("text")) | .text' "${RESPONSE_FILE}" > "${RAW_TEXT_FILE}"

  awk '
    BEGIN { in_diff = 0 }
    /^```diff[[:space:]]*$/ { in_diff = 1; next }
    /^```[[:space:]]*$/ { if (in_diff == 1) { exit } }
    { if (in_diff == 1) print }
  ' "${RAW_TEXT_FILE}" > "${PATCH_FILE}"

  if [[ ! -s "${PATCH_FILE}" ]]; then
    FEEDBACK="No valid diff block was returned. Return exactly one \`\`\`diff fenced block."
    TURN=$((TURN + 1))
    continue
  fi

  if rg -n '^(diff --git a/\.github/|diff --git b/\.github/|\+\+\+ b/\.github/|--- a/\.github/)' "${PATCH_FILE}" > "${RUN_DIR}/forbidden-paths.txt"; then
    FEEDBACK="Patch touched forbidden path .github/. Do not modify .github files."
    TURN=$((TURN + 1))
    continue
  fi

  if ! git apply --whitespace=fix "${PATCH_FILE}" > "${RUN_DIR}/git-apply-stdout.txt" 2> "${RUN_DIR}/git-apply-stderr.txt"; then
    FEEDBACK="$(cat "${RUN_DIR}/git-apply-stderr.txt")"
    TURN=$((TURN + 1))
    continue
  fi

  if bash -lc "${MODULE_TASK}" > "${TEST_OUTPUT_FILE}" 2>&1; then
    echo "Scoped validation passed on turn ${TURN}."
    exit 0
  fi

  FEEDBACK="$(tail -n 200 "${TEST_OUTPUT_FILE}")"
  TURN=$((TURN + 1))
done

echo "Weekly cleanup did not converge within ${MAX_TURNS} turns." >&2
exit 1
