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
- `.github/weekly-cleanup/cleanup-history.md` — skip candidates listed as `verified-in-use`; investigate `queued` candidates first before generating new ones.

### Turn Budget and History Priority
**The cleanup-history.md entry is the primary deliverable of every run.** A run that records "no safe target found" is a success. A run that exhausts all turns without writing history is a failure.

- **Write to `.github/weekly-cleanup/cleanup-history.md` immediately after each candidate verdict** — do not defer to the end.
- After evaluating **3 candidates** without finding a safe deletion target, stop investigating. Log any remaining identified-but-uninvestigated candidates as `queued` in history, then stop.
- If at any point you find yourself unsure how many turns remain, treat it as "low" — write history and stop rather than continue searching.

### Investigation Protocol
- Track findings as a ledger: record each candidate and its verdict (`deleted` / `verified-in-use` / `skipped`). Do not re-investigate a candidate once a verdict is reached.
- **Preferred investigation method for dead code candidates:** delete the symbol, run `__MODULE_TASK__ -q --console=plain 2>&1 | tail -50`. If it compiles clean, the deletion stands. If it fails, run `git checkout -- <file>` and mark the candidate `verified-in-use`. This is faster and more reliable than grep chains.
- Maximum candidates evaluated per run: **3**.
- If no safe target exists after evaluating candidates, stop without leaving any uncommitted deletions.

### Cleanup History
**Write this immediately after reaching each candidate verdict, not at the end.** Append one entry to `.github/weekly-cleanup/cleanup-history.md` per run, whether or not changes were made:

```
## __RUN_DATE__ — __FOCUS_AREA__
- <FileName.kt>: <verdict> — <one-line reason>
```

Verdicts: `deleted`, `verified-in-use`, `skipped` (out of scope or exceeded limits), `queued` (identified but not yet investigated — investigate these first in a future run).
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
