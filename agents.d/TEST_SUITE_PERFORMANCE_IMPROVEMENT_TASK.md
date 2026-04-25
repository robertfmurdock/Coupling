# Task: Semantics-preserving test suite performance improvement

Goal
- Reduce end-to-end test runtime and p95 command latency in `:sdk:jvmTest`, `:sdk:jsNodeTest`, and `:e2e:e2eRun`.
- Preserve current behavioral coverage and intended real-world usage semantics.
- Avoid invalidating existing tests or weakening assertions.

Hard constraints
- No test removals for performance.
- No assertion weakening or semantic downgrades.
- No replacing realistic command paths with mocks/fakes in integration-style tests unless already present by design.
- Any optimization must keep command/API flows representative of intended production usage.

What analysis says today (from `build/test-output/test.jsonl`)
- Main hot actions:
  - `DeletePartyCommand` is high-volume and high-latency, with heavy tail.
  - `ApplyBoostCommand` has severe tail latency in JS despite lower count.
  - `GqlQuery` is the highest-volume action and compounds total cost.
- Concurrency is very high in SDK test tasks:
  - many overlapping commands (max parallel ~54-59), which amplifies contention.
- Run-to-run variability is substantial in SDK tasks:
  - same command counts, but large p95/avg latency swings between runs.
- Current per-test command attribution is approximate under overlap (until explicit command-to-test tagging lands).

Optimization strategy
- Prioritize reducing contention and variance first; then target hottest command paths in test setup/teardown usage patterns.
- Focus on harness/data-shape/test-structure improvements that keep business semantics intact.
- Use log-derived metrics as acceptance gates for every slice.

Success metrics (initial targets)
- `:sdk:jsNodeTest`
  - reduce command p95 at task level by >=20% from current median baseline across 5 runs.
  - reduce `DeletePartyCommand` p95 by >=20%.
- `:sdk:jvmTest`
  - reduce command p95 at task level by >=15% from current median baseline across 5 runs.
  - reduce `DeletePartyCommand` p95 by >=15%.
- Stability
  - coefficient of variation for task-level avg command duration reduced by >=30% across repeated runs.
- Correctness
  - zero test regressions, zero strict analyzer violations.

Baseline protocol (must run before changes)
- Execute each target task 5 times with stable machine conditions; store analyzer JSON snapshots per run.
- Commands:
  - `./gradlew resetTestJsonl --no-configuration-cache`
  - repeat 5x:
    - `./gradlew -Pcoupling.testLog.reset=true :sdk:jsNodeTest :sdk:jvmTest --no-configuration-cache`
    - `./gradlew analyzeTestJsonl --no-configuration-cache > build/test-output/analyze-baseline-<n>.json`
- Produce baseline medians for:
  - task-level `avg_command_ms`, `p95_command_ms`
  - action-level p95 for `DeletePartyCommand`, `ApplyBoostCommand`, `GqlQuery`
  - run-to-run variance metrics

Rollout slices

- [ ] Slice 1 - reproducibility + measurement harness
  - Add a repeatable local script in `scripts/` to run N iterations and aggregate key metrics.
  - Report median/p95/min/max/CV by task and key actions.
  - Keep this script read-only against test semantics (measurement only).

- [ ] Slice 2 - reduce artificial contention in test orchestration (without reducing behavior)
  - Audit SDK test execution parallelism knobs (Gradle workers/test framework parallel settings).
  - Tune for lower contention while preserving total behavior coverage:
    - avoid globally serializing everything unless data proves it is best throughput.
    - prefer bounded concurrency over unbounded overlap.
  - Validate net wall-clock and p95 improvements from logs.

- [ ] Slice 3 - setup/teardown efficiency patterns in high-churn tests
  - Target suites with high `DeletePartyCommand`/`GqlQuery` counts.
  - Refactor repetitive setup flows into semantic-preserving fixtures/helpers that:
    - avoid unnecessary repeated create/delete cycles when same behavior can be asserted in one realistic flow.
    - preserve externally observable behavior and assertions.
  - Keep test intent explicit; no hidden shared mutable state across tests.

- [ ] Slice 4 - targeted hotspots in test-invoked command paths (non-semantic)
  - Investigate JS-heavy `ApplyBoostCommand` tail behavior under test load.
  - Investigate `DeletePartyCommand` tail under concurrent SDK tests.
  - Prefer low-risk improvements first:
    - request construction overhead
    - redundant query calls in test helper paths
    - avoid duplicate command invocations in fixtures
  - Do not change command semantics; only remove avoidable overhead.

- [ ] Slice 5 - variance hardening
  - Identify and reduce noisy factors in test runtime:
    - unnecessary retries/polls with aggressive intervals
    - unstable timing assumptions in asynchronous waits
    - expensive one-time initialization repeated per test when safely reusable
  - Validate improvement via 5-run trend comparison.

- [ ] Slice 6 - guardrails + regression gates
  - Add performance smoke gate in CI/local tooling:
    - compare current run metrics against rolling baseline envelope.
    - non-blocking warn first, then promote to thresholded failure once stable.
  - Keep correctness gates mandatory (`test`, `check`, analyzer strict).

Candidate first targets (based on current logs)
- `:sdk:jsNodeTest`:
  - `SdkPartyTest.deleteWillMakePartyInaccessible`
  - `SdkPartyTest.saveMultipleThenGetEachByIdWillReturnSavedParties`
  - `SdkPairs*` tests with very high command-call density
- `:sdk:jvmTest`:
  - `RequestCombineEndpointTest.postPlayersAndPinsThenGet()`
  - `SdkPairs*` and `SdkPlayer*` high command-density tests
- `:e2e:e2eRun`:
  - lower priority for raw speed (already relatively stable), but keep as regression check

Validation checklist for every slice
- Correctness:
  - `./gradlew test --no-configuration-cache`
  - `./gradlew analyzeTestJsonl --no-configuration-cache` (strict violations must remain 0)
- Performance:
  - at least 3 repeated runs on touched tasks
  - compare median and p95 vs pre-slice baseline
  - summarize gains/losses by task and key actions

Reporting template (per slice)
- Change summary
- Why semantics are preserved
- Before/after metrics (median and p95)
- Variance impact (CV/range)
- Residual risks

Open dependency
- Exact per-test command attribution is currently approximate under high overlap.
- The companion plan in `agents.d/TEST_COMMAND_TEST_ATTRIBUTION_TASK.md` should run in parallel or early to improve confidence in per-test targeting.

Definition of done
- Targeted command/task p95 reductions met (or thresholds revised with data-backed rationale).
- No correctness regressions.
- Performance measurement workflow is repeatable and documented.
- Team can identify bottlenecks from raw log-derived reports without bespoke manual analysis.

Continuation status
- checkpoint: working tree (uncommitted)
- next: `NEXT=SLICE_1_REPRODUCIBILITY_AND_MEASUREMENT_HARNESS`
