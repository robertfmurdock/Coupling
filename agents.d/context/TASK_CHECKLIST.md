# Task Checklist

Use this checklist for every implementation task.

## Intake
- Start with:
  - `./gradlew agentBootstrap`
  - If bootstrap fails with an AWS credential error, check whether `AWS_PROFILE`
    is set to a profile unrelated to this repo; `unset AWS_PROFILE` if so.
- Task artifacts live in `agents.d/tasks/`.
- Read:
  - Context files listed in `agents.d/context/context.json` (`required_reads`)
  - Relevant playbooks in `agents.d/context/context.json` (`playbooks`) based on task type
- Identify impacted modules and likely test scope.
- Confirm constraints and assumptions before coding.

## Implementation
- Keep changes focused on impacted modules.
- Follow existing patterns and module ownership.
- Update all linked artifacts for cross-layer changes.

## Validation
- Run smallest sufficient task set first.
- Use Gradle wrapper (`./gradlew`) only.
- For GraphQL changes, run `agents.d/utilities/graphql-ref-scan.sh` for text-reference
  discovery, then verify behavior with relevant Gradle tests/checks.

## Completion Report
- List files changed and intent.
- List validation commands run and results.
- Move completed task artifacts to `agents.d/tasks_completed/`.
- State residual risks, skipped checks, or follow-ups.
