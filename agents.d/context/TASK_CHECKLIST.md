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
- Define test-level intent up front:
  - primary property to prove,
  - highest architecture level needed for confidence-anchor coverage,
  - candidate variations/permutations to place at lower integration levels.
- Confirm constraints and assumptions before coding.

## Implementation
- If the task artifact has no `## Checklist` section, create one before writing any code.
  List each planned slice as an unchecked item (`- [ ] ...`). This is the first slice.
- Keep changes focused on impacted modules.
- Follow existing patterns and module ownership.
- Prefer existing libraries and build tooling over custom implementations.
- Update all linked artifacts for cross-layer changes.
- **Each slice or step must be integration-oriented**: the repository should be in a safe,
  check-in-ready state after every slice, so work can be paused and resumed at any slice boundary.
- **End every slice by marking it complete in the task artifact** (`agents.d/tasks/<TASK>.md`).
  Do not batch task file updates to the end — update as you go.

## Validation
- Run smallest sufficient task set first for quick feedback.
- Use Gradle wrapper (`./gradlew`) only.
- Before completing any task, run `./gradlew check` to verify no cross-module surprises — let the build tooling determine impact, do not anticipate it.
- Validate the test mix:
  - confidence-anchor coverage exists at the intended boundary level,
  - variation coverage is pushed downward where possible without reducing
    confidence.
- For GraphQL changes, run `agents.d/utilities/graphql-ref-scan.sh` for text-reference
  discovery, then verify behavior with relevant Gradle tests/checks.

## Completion Report
- List files changed and intent.
- List validation commands run and results.
- Confirm all slices are marked `[x]` in the task artifact, then move it to `agents.d/tasks_completed/`.
- State residual risks, skipped checks, or follow-ups.
