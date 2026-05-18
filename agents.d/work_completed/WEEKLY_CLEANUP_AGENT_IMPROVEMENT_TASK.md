# Task: Weekly Cleanup Agent — Effectiveness and Extensibility Improvements

## Goal

Make the weekly cleanup agent more effective per run and more extensible beyond dead-code
detection, so it can eventually identify and act on other forms of architectural misalignment.

## Problem Statement

The 2026-05-16 run (`libraries/model` focus) hit `error_max_turns` (40) without finding a
deletable candidate. Analysis of the run log identified:
- Most turns were spent on exploratory grep searching that a pre-computed candidate list
  could replace.
- The investigation protocol is dead-code-biased — search for imports, run TCR delete.
  Other cleanup types (boundary violations, naming inconsistency, test misplacement) have
  no equivalent first-class path.
- One history accuracy bug: the recorded entry says `NotEmptyExtensions.kt: verified-in-use`
  but the actual evaluated file was `NotEmptyFlatMap.kt`.
- Queued candidates were not written before the run hit the turn limit.

## Hard Constraints

- Every agent run must remain safe to merge as a draft PR or no-op; no behavior changes.
- Cleanup history must remain append-only and accurate (no rewrites of prior entries).
- New analysis strategies must respect the same file/line safety gates as dead code deletion.
- Pre-computation (Gradle tasks, utilities) must not require changes to CI workflow steps
  unless explicitly called out in a slice.

## Design Principle: Strategy-Based Investigation

The agent currently has one implicit investigation strategy: "search for dead code by import
scan." To support other misalignment types without rewriting the whole prompt each time,
refactor toward an explicit **strategy** concept:

- A strategy is a named investigation protocol with its own discovery heuristic and action
  path (e.g., TCR delete, rename, comment removal).
- The weekly plan can select a strategy (initially: rotate by week or derive from focus) in
  addition to a focus module.
- Each strategy produces typed findings in a consistent shape that cleanup-history.md can
  represent uniformly.
- Strategies are loaded as prompt includes or separate prompt sections — the main prompt
  becomes a thin harness that loads the appropriate strategy context.

Initial strategy set (not all implemented in this task):
- `dead-code` — current behavior (import scan + TCR delete). Refine, don't replace.
- `boundary-check` — identify server policy in client code, shared abstractions added
  prematurely, or cross-module policy violations per `BOUNDARIES.md`.
- `naming-consistency` — identify identifiers, file names, or module paths that diverge
  from established patterns in their neighborhood.
- `test-placement` — identify tests anchored at the wrong architectural level per
  `ARCHITECTURE_CANONICAL.md` testing strategy.
- `suggest-new-strategy` — meta-strategy: no cleanup; surveys for recurring misalignment
  patterns not yet covered and produces a new task artifact proposing a strategy to address
  them. Runs at lower frequency than active strategies.

## Slices

- [x] Slice 1 — Fix history accuracy bug
  - Root cause: TCR delete script recorded `NotEmptyExtensions.kt` instead of
    `NotEmptyFlatMap.kt` (the file actually passed to the script).
  - Investigate `agents.d/utilities/tcr-delete.sh`: confirm how the recorded filename is
    derived and whether it uses the argument or parses error output.
  - Fix the script if it has a bug; otherwise add a note to the cleanup-history entry.
  - Correct the 2026-05-16 history entry to reference `NotEmptyFlatMap.kt`.
  - Validation: dry-run the script against a known file; confirm the history line matches
    the argument.

- [x] Slice 2 — Turn budget and queuing fixes (quick wins)
  - Increase `WEEKLY_CLEANUP_MAX_TURNS` in the workflow from 40 to 60.
  - Update agent prompt: change the queuing rule from "after 3 candidates, queue the rest"
    to "write each newly discovered candidate as `queued` immediately upon discovery,
    before investigating it." This guarantees forward state even if the run is truncated.
  - Surface queued candidates in the rendered prompt: `WeeklyCleanupRenderPromptTask` should
    read the last N `queued` entries from `cleanup-history.md` and inject them at the top
    of the rendered prompt under a "**Start here:**" heading.
  - Validation: manually verify the rendered prompt for a run with queued history entries.

- [x] Slice 3 — Pre-agent candidate list for `dead-code` strategy
  - Add a Gradle task `weeklyCleanupCandidates` in `:scripts:weekly-cleanup` that, for a
    given focus path, produces `build/weekly-cleanup/candidates.md`: a ranked list of
    Kotlin files within the focus scope that appear zero times as import targets outside
    their own file, sorted by file size ascending (smallest = lowest blast radius first).
  - Wire it into the workflow as a step after `Prepare Prompt` and before `Run Cleanup
    Agent Command`; make the candidates file path available to the agent via the rendered
    prompt.
  - The agent prompt (dead-code strategy section) should instruct the agent to consume this
    file instead of grepping from scratch.
  - Validation: run `weeklyCleanupCandidates` against `libraries/model`; confirm output
    would have surfaced `NotEmptyFlatMap.kt` as a candidate.

- [x] Slice 4 — Refactor prompt into strategy-aware harness
  - Split `agent-prompt.md` into:
    - `agent-prompt-harness.md` — thin orchestration: context reads, turn budget rules,
      history write contract, scope/limits, decline conditions. Strategy-agnostic.
    - `agent-strategy-dead-code.md` — dead-code investigation protocol (import scan,
      TCR delete, candidate file consumption from Slice 3).
  - `WeeklyCleanupRenderPromptTask` selects and includes the appropriate strategy file
    based on a `STRATEGY` field in `plan.env` (initially always `dead-code`; future slices
    add rotation).
  - The plan task derives `STRATEGY` from a new `allowedStrategies` list rotated by week
    (or accepts a `weeklyCleanupStrategyOverride` Gradle property for dry runs).
  - No behavior change in this slice — same strategy, same prompt content, just restructured.
  - Validation: rendered prompt for `dead-code` strategy is byte-for-byte equivalent to
    current rendered prompt modulo whitespace.

- [x] Slice 5 — Add `boundary-check` strategy
  - Write `agent-strategy-boundary-check.md`:
    - Discovery: read `BOUNDARIES.md`; search focus module for imports or patterns that
      violate stated ownership rules.
    - Action path: for each violation, either delete the offending abstraction (if dead)
      or move it to the correct owner module (only if impact is provably local and
      `./gradlew check` passes).
    - History verdict shape: `<FileName.kt>: boundary-violation-fixed` or
      `boundary-violation-queued`.
  - Wire into strategy rotation.
  - Validation: run a dry-run against a known clean module; confirm no false positives.

- [x] Slice 6 — Add `suggest-new-strategy` meta-strategy
  - Write `agent-strategy-suggest-new-strategy.md`:
    - Purpose: when this strategy is selected, the agent does not perform any cleanup.
      Instead it surveys the codebase for patterns of misalignment that don't yet have a
      named strategy, and proposes one new strategy.
    - Discovery: read `ARCHITECTURE_CANONICAL.md`, `BOUNDARIES.md`, the current strategy
      files, and the focus module. Look for recurring violation patterns, inconsistencies,
      or technical debt shapes that a future agent could act on systematically.
    - Output: the agent creates a new work card under `agents.d/work/` named
      `CLEANUP_STRATEGY_<NAME>_CARD.md` describing the proposed strategy: what it detects,
      what action it takes, what the history verdict shape is, and what false-positive risks
      to guard against. It does not implement the strategy — that is deferred to a separate
      task.
    - History entry shape: `strategy-proposed: <strategy-name> — <one-line description>`
    - No code changes; this strategy is always a no-op on the working tree.
  - Wire into strategy rotation (lower frequency than active strategies — e.g., once every
    N weeks rather than on every focus cycle).
  - Validation: trigger a dry-run with `weeklyCleanupStrategyOverride=suggest-new-strategy`;
    confirm a task artifact is created and the history entry is written with no file changes.

## Affected Files (anticipated)

- `agents.d/utilities/tcr-delete.sh` — Slice 1
- `.github/weekly-cleanup/cleanup-history.md` — Slice 1 correction
- `.github/workflows/weekly-cleanup-pr.yml` — Slice 2, 3
- `.github/weekly-cleanup/agent-prompt.md` → split in Slice 4
- `scripts/weekly-cleanup/build.gradle.kts` — Slice 2, 3, 4
- New: `agents.d/context/agent-strategy-dead-code.md` — Slice 4
- New: `agents.d/context/agent-strategy-boundary-check.md` — Slice 5
- New: `agents.d/context/agent-strategy-suggest-new-strategy.md` — Slice 6

## Validation Checklist (per slice)

- Slice 1: TCR script dry-run + corrected history entry
- Slice 2: rendered prompt inspection for queued-entry injection; workflow YAML diff
- Slice 3: `weeklyCleanupCandidates` output for `libraries/model`; confirm `NotEmptyFlatMap.kt` surfaced
- Slice 4: diff of rendered prompt (before/after must match modulo whitespace)
- Slice 5: dry-run on clean module; no spurious violations
- Slice 6: dry-run with strategy override; confirm task artifact created, no working-tree changes
- All slices: `./gradlew :scripts:weekly-cleanup:check` (or equivalent) after Gradle changes

## Definition of Done

- History accuracy: recorded file names always match the argument passed to `tcr-delete.sh`.
- Queued candidates survive run truncation: any candidate discovered but not evaluated is in history before the run ends.
- Rendered prompt surfaces prior `queued` entries at the top.
- Agent turn budget is 60; pre-computed candidate list eliminates exploratory grep turns for `dead-code` strategy.
- Prompt is structured as a strategy-aware harness; adding a new strategy requires only a new strategy file + one-line plan task change.
- At least one non-dead-code active strategy (`boundary-check`) and one meta-strategy (`suggest-new-strategy`) are implemented and in rotation.
