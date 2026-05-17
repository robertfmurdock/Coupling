### Test Grooming Protocol

Evaluate whether tests are placed at the **right architectural level** per the
confidence-anchor philosophy in `ARCHITECTURE_CANONICAL.md`. This strategy
targets redundancy, not dead code: tests execute and pass — the question is
whether they provide the *right kind* of confidence at the *right boundary*.

#### Candidate List

A pre-computed list of test files in the focus area is available at
`__CANDIDATES_FILE__`. Read it first and use it as your starting point.
Pick candidates from this list — do not scan for test files from scratch.

#### Discovery Heuristic (apply to each candidate)

1. **What boundary does this test verify?**
   Internal logic, module API, cross-module composition, GraphQL contract, or e2e behavior.

2. **Is there a higher-level test that verifies the same primary behavioral property?**
   If yes, cite it by full file path and test function name.

3. **Does the candidate add variations, edge cases, or permutations not present
   in the higher-level coverage?**
   If yes → `verified-anchor-or-variation`. Stop.

4. A candidate is actionable **only when** (2) is answered with a concrete
   citation AND (3) is answered "no."

#### Written Justification Required Before Any Deletion

Before invoking `tcr-delete.sh`, write:

1. What behavioral intent this test verifies.
2. The specific file(s) covering that intent at a higher level (full path, test name).
3. Why the candidate adds no variation or edge case not in the cited coverage.
4. How this aligns with the confidence-anchor strategy.

If any of the four cannot be answered concretely, record the candidate as
`test-grooming-gap` or `test-grooming-ambiguous` and stop.

#### Action Paths

- **Redundant lower-level test (concrete higher-level anchor exists, no unique variation):**
  Write the four-point justification, then use `tcr-delete.sh`. If build fails,
  record `test-grooming-revert`.
- **Under-integrated anchor (behavior crosses boundary, no higher-level test exists):**
  Record `test-grooming-gap` — do not delete; note the missing higher-level test.
- **Over-integrated unit test (integration harness for pure-logic test):**
  Record `test-grooming-move-candidate` with target level noted. Do not move unless
  the move is trivially mechanical within this run.
- **Orphaned variation (confidence anchor was removed):**
  Record `test-grooming-orphan` with the missing anchor noted.
- **Confidence anchor or variation (adds unique edge cases):**
  Record `verified-anchor-or-variation`. Retain.

#### History Verdicts

Append to `.github/weekly-cleanup/cleanup-history.md` immediately after each verdict:

```
- <TestFile.kt>: test-grooming-deleted — redundant; <PrimaryTestFile.kt> covers same behavior at <level>
- <TestFile.kt>: test-grooming-revert — deletion attempted; build failed; behavioral gap confirmed
- <TestFile.kt>: test-grooming-gap — no higher-level anchor exists; deletion would remove coverage
- <TestFile.kt>: test-grooming-move-candidate — over-integrated; target level: <unit|integration|e2e>
- <TestFile.kt>: test-grooming-orphan — variation test; its confidence anchor is missing
- <TestFile.kt>: verified-anchor-or-variation — adds edge cases not covered at higher level; retain
```

#### False-Positive Guards

- "Nothing imports it" is **not** a signal of redundancy. Test files are auto-discovered.
- Build passing after deletion is **not** proof of redundancy. It only proves remaining tests pass.
- Same function name ≠ same behavioral intent. A higher-level test calling a function as
  part of a larger flow may not assert the same edge cases as a focused unit test.

#### Scope

- Maximum candidates evaluated **this session**: **3**.
- Do not delete confidence-anchor tests — only redundant lower-level duplicates.
- Do not attempt test moves across `client/` ↔ `server/` boundaries.
- Do not modify test assertions.
