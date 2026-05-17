## Weekly Cleanup Prompt

You are proposing one small, architecture-aligned cleanup in this repository.

### Required Context Reads
Read these before making changes:
- `agents.d/context/ARCHITECTURE_CANONICAL.md`
- `agents.d/context/BOUNDARIES.md`
- `agents.d/context/PERSONA.md`
- `agents.d/context/TASK_CHECKLIST.md`
- `agents.d/context/generated/repo-index.md`
- `agents.d/context/generated/workflows.md`
- `agents.d/context/GRADLE_PLAYBOOK.md`
- `agents.d/context/context.json`

### Scope
- Focus area: `__FOCUS_AREA__`
- Date: `__RUN_DATE__`
- Change limit: at most `5` files and `200` changed lines.
- Keep cleanup in one bounded area only.
- Allowed cleanup categories:
  - dead code deletion with local tests
  - naming/consistency cleanup without behavior changes
  - small boundary cleanup in owner module
  - test clarity cleanup (fixtures/duplication)
  - Gradle hygiene that preserves behavior

### Non-goals
- No broad refactors.
- No product behavior changes.
- No API schema shifts unless fully synchronized across server/sdk/tests in one change set.

### Validation
- Use Gradle wrapper commands only.
- Run `./gradlew agentBootstrap` first.
- Run scoped checks for impacted module(s), preferring exactly one of:
  - `./gradlew :module:check`
  - `./gradlew :module:test`

### PR Content
Output changes suitable for:
- branch: `bot/cleanup/__FOCUS_AREA__/__RUN_DATE__`
- title: `chore(cleanup): __FOCUS_AREA__ architecture-aligned cleanup`
- body sections:
  1. What changed
  2. Architecture alignment
  3. Validation commands and results
  4. Risk and rollback

### Decline Conditions
If no safe in-scope cleanup is found:
- make no code changes
- write a short run summary describing why no PR was created
