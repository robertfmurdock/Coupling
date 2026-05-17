# Task: Test Grooming Strategy

## What It Detects

Tests that are placed at the wrong architectural level per the confidence-anchor
philosophy in `ARCHITECTURE_CANONICAL.md`. This is distinct from dead code: the
tests execute and pass — the question is whether they provide the *right kind* of
confidence at the *right boundary*.

Patterns to detect:

- **Over-integrated unit tests**: a test placed in an integration or e2e harness
  that only exercises a single module's internal logic. The behavior would be better
  anchored at a unit/common-test level for faster feedback.
- **Under-integrated confidence anchors**: behavior that crosses a module boundary
  (e.g., a GraphQL resolver calling a command path) but is only tested at an
  internal unit level. No test verifies the cross-boundary composition.
- **Redundant lower-level tests**: a unit test that duplicates assertions already
  made by a higher-level integration or e2e test, without adding any variation or
  edge case not present at the higher level.
- **Orphaned variation tests**: edge-case or permutation tests whose confidence
  anchor (the primary behavioral test at the highest appropriate level) was removed
  or moved, leaving the variations without a covering anchor.

Concrete example: `CouplingPairTest.kt` in
`libraries/model/src/commonTest/kotlin/.../pairassignmentdocument/` tests
`toCouplingPair()` and `equivalent()` — both functions heavily used across the
codebase. Before deleting, the correct question is: *at what architectural level
is the behavior of these functions verified as part of the system's primary
behavioral properties?* If the answer is "nowhere above unit level," deletion is
wrong; the test is a confidence anchor even if no other file imports it.

## Discovery Heuristic

1. Read `agents.d/context/ARCHITECTURE_CANONICAL.md` — focus on Testing Level
   Strategy and Module Ownership / Tests bullet.
2. For the focus module, enumerate all test files (files in `*Test*` paths or
   named `*Test.kt`).
3. For each candidate, determine:
   a. What boundary does this test verify? (internal logic, module API,
      cross-module composition, GraphQL contract, e2e behavior)
   b. Is there a higher-level test that verifies the same primary behavioral
      property? If yes, cite it by file path.
   c. Does the candidate add variations, edge cases, or permutations not
      present in the higher-level coverage? If yes, it is not a redundancy
      candidate — mark `verified-anchor-or-variation` and stop.
4. A candidate is actionable only when both (b) is answered with a concrete
   citation and (c) is answered "no."

## Action Path

This strategy does **not** use `tcr-delete.sh` as its primary tool.

- **Redundant lower-level test with confirmed higher-level anchor**: propose
  deletion with a written justification citing (b). Only then use
  `tcr-delete.sh` to validate. If the build fails, record
  `test-grooming-revert` and stop.
- **Under-integrated confidence anchor** (behavior crosses boundary but no
  higher-level test exists): record as `test-grooming-gap` — do not delete;
  note the missing higher-level test as a follow-up.
- **Over-integrated unit test** (integration test harness for pure logic):
  record as `test-grooming-move-candidate` with the target level noted. Do
  not move within this run unless the move is trivially mechanical (same
  assertions, different source set, no harness dependency changes).
- **Orphaned variation**: record as `test-grooming-orphan` with the missing
  anchor noted as a follow-up.

## Written Justification Required Before Any Deletion

Before invoking `tcr-delete.sh` on any test file under this strategy, write:

1. What behavioral intent this test verifies.
2. The specific file(s) that cover that intent at a higher architectural level
   (full path, function/test name if identifiable).
3. Why the candidate adds no variation or edge case not already present in
   the cited higher-level coverage.
4. How this aligns with the confidence-anchor strategy: the primary property
   remains anchored at the highest appropriate level; this candidate is a
   pure duplicate below that level.

If any of these four points cannot be answered concretely, record the
candidate as `test-grooming-gap` or `test-grooming-ambiguous` and stop.

## History Verdict Shape

```
- <TestFile.kt>: test-grooming-deleted — redundant; <PrimaryTestFile.kt> covers same behavior at <level>
- <TestFile.kt>: test-grooming-revert — deletion attempted; build failed; behavioral gap confirmed
- <TestFile.kt>: test-grooming-gap — no higher-level anchor exists; deletion would remove coverage
- <TestFile.kt>: test-grooming-move-candidate — over-integrated; target level: <unit|integration|e2e>
- <TestFile.kt>: test-grooming-orphan — variation test; its confidence anchor is missing
- <TestFile.kt>: verified-anchor-or-variation — adds edge cases not covered at higher level; retain
```

## False-Positive Risks

- **"Nothing imports it" is not a signal of redundancy.** Test files are
  auto-discovered; absence of import references means nothing about whether
  the test executes or whether its assertions are unique.
- **Build passing after deletion is not proof of redundancy.** It only means
  remaining tests still pass. It says nothing about whether the deleted
  assertions covered behavior not tested elsewhere.
- **Same function name ≠ same behavioral intent.** A higher-level test that
  calls `toCouplingPair()` as part of a larger flow may not assert the same
  edge cases as a focused unit test.

## Scope Guards

- Maximum candidates evaluated per run: **3** (same as dead-code strategy).
- Do not delete confidence-anchor tests (the single highest-level behavioral
  test for a primary property) under this strategy. Only redundant lower-level
  duplicates are in scope.
- Do not attempt test moves across `client/` ↔ `server/` boundaries; those
  require explicit cross-layer planning.
- Do not modify test assertions — this strategy is placement/redundancy only.

## Checklist

- [x] Strategy document complete (this file)
- [x] Create `agents.d/context/agent-strategy-test-grooming.md` (agent-facing prompt, distilled from this document)
- [x] Add `test-grooming` to `allowedStrategies` and `strategyRotation` in `scripts/weekly-cleanup/build.gradle.kts`
- [x] Update `WeeklyCleanupCandidatesTask` to generate test-file candidates when strategy is `test-grooming`
- [x] Run `./gradlew :scripts:weekly-cleanup:weeklyCleanupPlan -PweeklyCleanupStrategyOverride=test-grooming` and verify plan output
- [x] Run `./gradlew :scripts:weekly-cleanup:weeklyCleanupCandidates` and verify candidates file lists test files
- [x] Run `./gradlew :scripts:weekly-cleanup:weeklyCleanupRenderPrompt` and verify rendered prompt includes strategy content
- [ ] Move this file to `agents.d/tasks_completed/`
