# Weekly Cleanup PR Automation

This directory contains policy and operator notes for the scheduled cleanup PR workflow.

## Files
- `agent-prompt.md`: prompt template used by the workflow for the cleanup agent.

## Workflow
- Workflow file: `.github/workflows/weekly-cleanup-pr.yml`
- Trigger:
  - Weekly schedule (`0 15 * * 1`)
  - Manual (`workflow_dispatch`)
- Pilot mode:
  - Draft PRs only.
  - Strict safety gates (`<= 5 files`, `<= 200 changed lines`, focus-area allowlist).

## Local Dry Run
- `./gradlew :scripts:weekly-cleanup:weeklyCleanupPlan`
- `./gradlew :scripts:weekly-cleanup:weeklyCleanupRenderPrompt`
- `BEDROCK_MODEL_ID=<model-id> ./scripts/weekly-cleanup/run-bedrock-cleanup.sh`
- `./gradlew :scripts:weekly-cleanup:weeklyCleanupEvaluate`

## Configuration
- Required repository variable for live agent execution:
  - `WEEKLY_CLEANUP_BEDROCK_MODEL_ID`
    - Bedrock model id used by `scripts/weekly-cleanup/run-bedrock-cleanup.sh`.
    - Example: `anthropic.claude-3-5-sonnet-20241022-v2:0`
    - If not configured, workflow exits with a no-op summary and does not open a PR.
- AWS auth:
  - Workflow uses GitHub OIDC with `aws-actions/configure-aws-credentials`.
  - Current role in workflow: `arn:aws:iam::174159267544:role/LocalDevelopment`.

## Focus Rotation
1. `client/components`
2. `sdk`
3. `server/actionz`
4. `libraries/model`
5. `libraries/repository/core`
6. `e2e`

Focus is selected by ISO week-of-year modulo rotation length.

## Tuning
- To tighten scope, lower the file/line limits in workflow env.
- To change validation depth, update `focus -> Gradle task` mapping.
- Promote from draft mode after pilot metrics meet threshold (merge rate, low churn, low regression rate).
