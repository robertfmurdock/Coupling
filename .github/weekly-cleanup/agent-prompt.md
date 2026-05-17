## Weekly Cleanup Prompt

You are performing one small, architecture-aligned cleanup directly in this repository.
You have full tool access: read files, explore the codebase, and edit files directly.

### CI Context Overrides
- `./gradlew agentBootstrap` and `./gradlew check` were already run by the workflow before this prompt — do not run them again at the start.
- The full Gradle build cache is warm — targeted and full validation runs are fast.
- **Validation strategy** (in order):
  1. During investigation and after each change, run targeted tests for quick feedback:
     `__MODULE_TASK__ -q --console=plain 2>&1 | tail -50`
  2. Before finishing, run a full build to catch any cross-module surprises:
     `./gradlew check -q --console=plain 2>&1 | tail -100`
  - The `-q` flag suppresses download noise. Only read detailed output if a command fails.
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
- `.github/weekly-cleanup/cleanup-history.md` — skip any candidate listed as `verified-in-use` here.

### Investigation Protocol
- Complete all investigation **before** making any file changes.
- Track findings as a ledger: record each candidate and its verdict (`deleted` / `verified-in-use` / `skipped`). Do not re-investigate a candidate once a verdict is reached.
- After investigation, select exactly **one** cleanup target. If no safe target exists, stop without changing code.
- Make changes, validate once with the command above, stop. Do not loop back to re-investigate alternatives.
- Maximum investigation depth: 6 tool calls per candidate.

### Cleanup History
At the end of every run — whether or not changes were made — append one entry to `.github/weekly-cleanup/cleanup-history.md` in this format:

```
## __RUN_DATE__ — __FOCUS_AREA__
- <FileName.kt>: <verdict> — <one-line reason>
```

Verdicts: `deleted`, `verified-in-use`, `skipped` (out of scope or exceeded limits).
Keep each line under 120 characters. Do not rewrite prior entries.

### Scope
- Focus area: `__FOCUS_AREA__` — this is an **entrypoint for investigation**, not a hard boundary.
  Start here, but follow the trail where it leads. Let Gradle determine the actual impact of any change — do not try to anticipate cross-module effects yourself.
- Date: `__RUN_DATE__`
- Change limit: at most `__MAX_FILES__` files and `__MAX_LINES__` changed lines.
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
