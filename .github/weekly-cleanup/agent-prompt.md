## Weekly Cleanup Prompt

You are performing one small, architecture-aligned cleanup directly in this repository.
You have full tool access: read files, explore the codebase, and edit files directly.

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
- Do not run validation commands — the workflow runs them after your changes.

### Decline Conditions
If no safe in-scope cleanup is found, make no code changes.
