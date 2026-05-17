## Weekly Cleanup Prompt

You are performing one small, architecture-aligned cleanup directly in this repository.
You have full tool access: read files, explore the codebase, and edit files directly.

### CI Context Overrides
- `./gradlew agentBootstrap` was already run by the workflow before this prompt — do not run it again.
- The Gradle build cache is pre-warmed for `__MODULE_TASK__` — validation runs fast.
- To validate changes, run: `__MODULE_TASK__ -q --console=plain 2>&1 | tail -50`
  - The `-q` flag suppresses download noise; `tail -50` keeps output small.
  - Only read detailed output if the command fails.
- These override the general CLAUDE.md execution norms for this automated context.

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

### Decline Conditions
If no safe in-scope cleanup is found, make no code changes.
