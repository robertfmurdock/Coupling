### Suggest-New-Strategy Protocol

This is a **meta-strategy**: it performs no cleanup and makes no code changes. Its sole purpose is to
survey the codebase for recurring misalignment patterns that don't yet have a named cleanup strategy,
and to propose one new strategy that a future agent run could act on systematically.

#### Discovery

1. Read the following context files:
   - `agents.d/context/ARCHITECTURE_CANONICAL.md`
   - `agents.d/context/BOUNDARIES.md`
   - `agents.d/context/agent-strategy-dead-code.md`
   - `agents.d/context/agent-strategy-boundary-check.md`
   - `.github/weekly-cleanup/cleanup-history.md` — look for patterns in `queued` and `verified-in-use`
     verdicts that suggest a recurring problem type not addressed by existing strategies.
2. Scan the focus module for recurring structural patterns that are not covered by `dead-code` or
   `boundary-check`:
   - Naming inconsistencies (identifiers, file names, or module paths that diverge from neighborhood patterns)
   - Test misplacement (tests anchored at the wrong architectural level per the testing strategy in `ARCHITECTURE_CANONICAL.md`)
   - Redundant abstractions (multiple nearly-identical implementations with no convergence path)
   - Stale comments or outdated documentation embedded in source files
   - Any other recurring shape of technical debt visible in the focus module

#### Output

Identify the single most actionable recurring pattern you observe. Create a new task artifact at:
```
agents.d/tasks/CLEANUP_STRATEGY_<NAME>_TASK.md
```

Where `<NAME>` is a short uppercase identifier for the strategy (e.g., `NAMING_CONSISTENCY`,
`TEST_PLACEMENT`, `REDUNDANT_ABSTRACTIONS`).

The task artifact must describe:
- **What it detects**: the violation pattern, with at least one concrete example from the current focus module
- **Discovery heuristic**: how a future agent would find instances (grep pattern, structural scan, etc.)
- **Action path**: what the agent does upon finding an instance (rename, delete, move, annotate)
- **History verdict shape**: the exact string format for cleanup-history.md entries
- **False-positive risks**: conditions where the heuristic fires but no action should be taken, and how to distinguish them
- **Scope guards**: any file/line limits or off-limits boundaries specific to this strategy

#### History Entry

After creating the task artifact, append to `.github/weekly-cleanup/cleanup-history.md`:

```
## __RUN_DATE__ — __FOCUS_AREA__
- strategy-proposed: <strategy-name> — <one-line description of the proposed strategy>
```

#### Constraints

- **No code changes**: this strategy must never modify source files, test files, or build files.
- The task artifact is the only output; no implementation is attempted.
- If no actionable recurring pattern is found, write a history entry explaining what was surveyed and
  why no new strategy was proposed: `strategy-proposed: none — <reason>`.
