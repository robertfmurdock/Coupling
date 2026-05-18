# Task: Weekly LLM Cleanup PR Workflow (GitHub Actions)

## Completion notes
- Implemented in:
  - `.github/workflows/weekly-cleanup-pr.yml`
  - `.github/weekly-cleanup/agent-prompt.md`
  - `.github/weekly-cleanup/README.md`
- Uses weekly rotation, draft PR pilot mode, no-op success path, scoped Gradle validation, and explicit safety gates.

## Goal
- Create a scheduled GitHub Actions workflow that opens a cleanup PR once per week.
- Each run targets one bounded area of the codebase (rotating focus) and proposes architecture-aligned improvements.
- Keep changes safe, small, test-backed, and reviewable.

## Desired outcome
- A bot-authored PR appears weekly with:
  - a scoped cleanup change set
  - architecture rationale tied to canonical context
  - validation results from Gradle tasks
  - explicit risk notes and rollback simplicity

## Hard constraints
- Use Gradle wrapper commands only.
- Respect repository context contract in `agents.d/context/context.json`:
  - required reads before proposing changes
  - playbooks applied by task type
- Keep changes in one focus area per PR.
- Avoid broad refactors; optimize for reviewer clarity and merge safety.
- PR must include tests/checks for touched modules.

## Architecture alignment source
- Use these as the optimization goals for suggestions:
  - `agents.d/context/ARCHITECTURE_CANONICAL.md`
  - `agents.d/context/BOUNDARIES.md`
  - `agents.d/context/TASK_CHECKLIST.md`
  - optional: `agents.d/context/GRADLE_PLAYBOOK.md`, `agents.d/context/PLAYBOOK_GRAPHQL.md`

## Weekly focus model (rotation)
- Define a stable rotation list (example):
  1. `client/components`
  2. `sdk`
  3. `server/actionz`
  4. `libraries/model`
  5. `libraries/repository/core`
  6. `e2e`
- Derive focus from week-of-year modulo rotation length.
- Include current focus area in branch name and PR title.

## Candidate cleanup types (safe-first)
- Dead code deletion (unused helpers, stale wrappers) with local test coverage.
- Naming/consistency cleanup where behavior is unchanged.
- Small boundary improvements (move logic to owning module, reduce cross-module leakage).
- Test clarity upgrades for brittle/duplicated fixture patterns.
- Gradle task/dependency hygiene that preserves behavior.

## Non-goals (for this automation)
- Multi-module architecture rewrites in one PR.
- Large API shifts without full cross-layer sync.
- Behavior-changing product logic unless explicitly approved.

## Proposed rollout slices

- [ ] Slice 1 - Prompt + policy design
  - Create an agent prompt template that includes:
    - required context reads
    - weekly focus area
    - allowed cleanup categories
    - max file/change limits
    - mandatory validation commands
  - Add explicit "decline conditions" (no safe change found => open issue/comment instead of PR).

- [ ] Slice 2 - Workflow skeleton (`.github/workflows/weekly-cleanup-pr.yml`)
  - Trigger:
    - `schedule` (weekly)
    - `workflow_dispatch` (manual test)
  - Steps:
    - checkout
    - toolchain setup
    - derive focus area
    - run agent task with prompt
    - run scoped Gradle checks
    - open/update PR

- [ ] Slice 3 - Branching and PR conventions
  - Branch name format:
    - `bot/cleanup/<focus>/<yyyy-mm-dd>`
  - PR title format:
    - `chore(cleanup): <focus> architecture-aligned cleanup`
  - PR body sections:
    - what changed
    - why this aligns with architecture goals
    - validation commands/results
    - risk + rollback

- [ ] Slice 4 - Safety gates
  - Gate on:
    - file-count limit
    - changed-line limit
    - touched-path allowlist bound to focus area
    - required test/check success
  - If any gate fails:
    - do not open PR
    - publish run summary with reasons

- [ ] Slice 5 - Pilot mode (2-4 weeks)
  - Start with draft PRs only.
  - Review acceptance rate, churn, and revert rate.
  - Tune prompt constraints and focus rotation based on review feedback.

- [ ] Slice 6 - Steady-state operation
  - Promote from draft to normal PR only after pilot meets quality threshold.
  - Track metrics monthly:
    - merge rate
    - median review comments
    - post-merge regressions
    - cycle time

## Suggested first implementation ideas
1. Start with one conservative focus area (`libraries/model`) for first 2 runs.
2. Restrict edits to <= 5 files and <= 200 changed lines.
3. Require exactly one of:
   - `./gradlew :module:check`
   - `./gradlew :module:test`
   chosen from impacted module(s), plus any directly dependent checks if touched.
4. Force draft PR creation during pilot.
5. Add a "no-op success" path: if no safe cleanup identified, workflow completes with a summary and no PR.

## Validation plan
- Dry-run locally (or via `workflow_dispatch`) with fixed focus area.
- Confirm:
  - branch naming is stable
  - PR body contains required sections
  - gating prevents out-of-scope edits
  - Gradle checks run through wrapper
- Pilot acceptance criteria:
  - no broken mainline from bot PRs
  - >=50% of PRs merged without major rewrite requests

## Completion checklist
- [x] Workflow file added and runnable via `workflow_dispatch`.
- [x] Prompt/policy template checked in and referenced by workflow.
- [x] Focus rotation logic implemented.
- [x] Safety gates implemented and tested.
- [x] Draft PR mode enabled for pilot.
- [x] README/agents note added for maintainers on operating/tuning the bot.
