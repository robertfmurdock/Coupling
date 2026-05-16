# Task Checklist

Use this checklist for every implementation task.

## Intake
- Read:
  - `agents.d/context/ARCHITECTURE_CANONICAL.md`
  - `agents.d/context/BOUNDARIES.md`
  - `agents.d/context/PLAYBOOK_GRAPHQL.md` (when GraphQL-related)
- Identify impacted modules and likely test scope.
- Confirm constraints and assumptions before coding.

## Implementation
- Keep changes focused on impacted modules.
- Follow existing patterns and module ownership.
- Update all linked artifacts for cross-layer changes.

## Validation
- Run smallest sufficient task set first.
- Use Gradle wrapper (`./gradlew`) only.
- For GraphQL changes, run `scripts/graphql-ref-check.sh`.

## Completion Report
- List files changed and intent.
- List validation commands run and results.
- State residual risks, skipped checks, or follow-ups.

