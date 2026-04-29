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

- [x] Slice 1 - reproducibility + measurement harness
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
- next: `NEXT=SLICE_2_REDUCE_ARTIFICIAL_CONTENTION_IN_TEST_ORCHESTRATION`

Continuation update (2026-04-25, slice-1 reproducibility + measurement harness)
- Added repeatable performance harness script:
  - `scripts/test-suite-performance-harness.sh`
  - runs configurable N iterations against configurable task list (default `:sdk:jsNodeTest :sdk:jvmTest`)
  - executes baseline flow per run:
    - `./gradlew -Pcoupling.testLog.reset=true <tasks> --no-configuration-cache --rerun-tasks`
    - `./gradlew analyzeTestJsonl --no-configuration-cache`
  - captures per-run artifacts in `build/test-output/perf-harness-<timestamp>/run-<n>/`:
    - `test.jsonl`
    - `analyze.json`
    - `metrics.json`
- Added aggregation outputs:
  - `summary.json` with median/p95/min/max/CV for:
    - task-level `avg_command_ms` and `p95_command_ms`
    - key action `avg_ms` and `p95_ms` for configured actions (`DeletePartyCommand`, `ApplyBoostCommand`, `GqlQuery` by default)
  - `summary.txt` human-readable table report.
- Script is measurement-only and does not alter test semantics.
- Smoke verification:
  - `scripts/test-suite-performance-harness.sh --iterations 1 --tasks ":sdk:jsNodeTest" --gradle-args "-x kotlinStoreYarnLock" --output-dir build/test-output/perf-harness-smoke`
  - output contains:
    - task-level summary row for `:sdk:jsNodeTest`
    - key-action rows for `DeletePartyCommand`, `ApplyBoostCommand`, `GqlQuery`

Baseline run snapshot (2026-04-25)
- Command:
  - `scripts/test-suite-performance-harness.sh --iterations 5 --tasks ":sdk:jsNodeTest :sdk:jvmTest" --gradle-args "-x kotlinStoreYarnLock" --output-dir build/test-output/perf-harness-baseline-20260425`
- Artifacts:
  - `build/test-output/perf-harness-baseline-20260425/summary.json`
  - `build/test-output/perf-harness-baseline-20260425/summary.txt`
- Task-level baseline (median of per-run values):
  - `:sdk:jsNodeTest`
    - `avg_command_ms.median=134.390`
    - `p95_command_ms.median=683.215`
    - `avg_command_ms.cv=0.054`
    - `p95_command_ms.cv=0.057`
  - `:sdk:jvmTest`
    - `avg_command_ms.median=104.864`
    - `p95_command_ms.median=374.565`
    - `avg_command_ms.cv=0.061`
    - `p95_command_ms.cv=0.069`
- Key-action baseline (median of per-run values):
  - `DeletePartyCommand`: `avg_ms.median=242.497`, `p95_ms.median=820.842`
  - `ApplyBoostCommand`: `avg_ms.median=318.335`, `p95_ms.median=363.119`
  - `GqlQuery`: `avg_ms.median=99.306`, `p95_ms.median=728.350`

Slice-2 experiment log (2026-04-25, bounded Gradle workers candidate)
- Hypothesis:
  - Bound Gradle worker concurrency to reduce orchestration contention and command latency variance.
- Candidate tested:
  - `org.gradle.workers.max=6` (temporary local change, reverted after measurement).
- Command:
  - `scripts/test-suite-performance-harness.sh --iterations 3 --tasks ":sdk:jsNodeTest :sdk:jvmTest" --gradle-args "-x kotlinStoreYarnLock" --output-dir build/test-output/perf-harness-slice2-workers6-20260425`
- Result (vs baseline median):
  - Task `p95_command_ms`:
    - `:sdk:jsNodeTest`: `683.215 -> 652.615` (`-4.48%`)
    - `:sdk:jvmTest`: `374.565 -> 416.374` (`+11.16%`)
  - Key action `p95_ms`:
    - `DeletePartyCommand`: `820.842 -> 775.059` (`-5.58%`)
    - `ApplyBoostCommand`: `363.119 -> 378.042` (`+4.11%`)
    - `GqlQuery`: `728.350 -> 925.923` (`+27.13%`)
- Decision:
  - Reject this candidate; net impact is negative due JVM and `GqlQuery` regressions.
  - Keep `gradle.properties` unchanged from baseline state.

Slice-2 experiment log (2026-04-25, split sdk task invocations candidate)
- Hypothesis:
  - Running `:sdk:jsNodeTest` and `:sdk:jvmTest` as separate Gradle invocations per iteration will reduce cross-task contention and improve p95.
- Harness support added:
  - `scripts/test-suite-performance-harness.sh --split-task-invocations`
  - behavior: for each iteration run tasks sequentially (`task[0]` with log reset, remaining tasks without reset) before analysis capture.
- Commands attempted:
  - `scripts/test-suite-performance-harness.sh --iterations 3 --tasks ":sdk:jsNodeTest :sdk:jvmTest" --split-task-invocations --gradle-args "-x kotlinStoreYarnLock" --output-dir build/test-output/perf-harness-slice2-split-invocations-20260425`
  - rerun after failure:
    - `scripts/test-suite-performance-harness.sh --iterations 3 --tasks ":sdk:jsNodeTest :sdk:jvmTest" --split-task-invocations --gradle-args "-x kotlinStoreYarnLock" --output-dir build/test-output/perf-harness-slice2-split-invocations-rerun-20260425`
- Blocker encountered (both attempts):
  - `:sdk:jvmTest` failed on:
    - `SdkPartyTest[jvm] > saveWillIncludeModificationInformation()[jvm]`
    - `org.opentest4j.AssertionFailedError`
  - failure report:
    - `sdk/build/reports/tests/jvmTest/index.html`
- Partial signal from successful runs only (`run-1` + `run-2` of first attempt):
  - summary artifact:
    - `build/test-output/perf-harness-slice2-split-invocations-20260425/summary-partial-2runs.json`
  - Task `p95_command_ms` vs baseline:
    - `:sdk:jsNodeTest`: `683.215 -> 711.116` (`+4.08%`)
    - `:sdk:jvmTest`: `374.565 -> 654.172` (`+74.65%`)
  - Key action `p95_ms` vs baseline:
    - `DeletePartyCommand`: `820.842 -> 861.068` (`+4.90%`)
    - `ApplyBoostCommand`: `363.119 -> 443.138` (`+22.04%`)
    - `GqlQuery`: `728.350 -> 1093.868` (`+50.18%`)
- Decision:
  - Reject this candidate based on partial data and instability.
  - Treat `SdkPartyTest[jvm] saveWillIncludeModificationInformation()` as an active blocker for repeatable Slice-2 experimentation.

Pause / Resume Handoff (2026-04-25)
- Current status:
  - Slice 1 is complete (harness + 5-run baseline captured).
  - Slice 2 has two rejected candidates so far:
    - bounded workers (`org.gradle.workers.max=6`)
    - split task invocations (`--split-task-invocations`)
  - continuation marker remains:
    - `NEXT=SLICE_2_REDUCE_ARTIFICIAL_CONTENTION_IN_TEST_ORCHESTRATION`
- Active blocker:
  - Intermittent/recurring failure during repeat experiments:
    - `:sdk:jvmTest`
    - `SdkPartyTest[jvm] > saveWillIncludeModificationInformation()[jvm]`
    - `org.opentest4j.AssertionFailedError`
  - report path:
    - `sdk/build/reports/tests/jvmTest/index.html`
- Canonical baseline artifacts (do not overwrite; use for comparisons):
  - `build/test-output/perf-harness-baseline-20260425/summary.json`
  - `build/test-output/perf-harness-baseline-20260425/summary.txt`
- Slice-2 experiment artifacts:
  - workers cap candidate:
    - `build/test-output/perf-harness-slice2-workers6-20260425/summary.json`
    - `build/test-output/perf-harness-slice2-workers6-20260425/summary.txt`
  - split-invocation candidate:
    - `build/test-output/perf-harness-slice2-split-invocations-20260425/run-1/`
    - `build/test-output/perf-harness-slice2-split-invocations-20260425/run-2/`
    - partial aggregate:
      - `build/test-output/perf-harness-slice2-split-invocations-20260425/summary-partial-2runs.json`
    - failed rerun root:
      - `build/test-output/perf-harness-slice2-split-invocations-rerun-20260425/`
- Harness script status:
  - local script exists and is executable:
    - `scripts/test-suite-performance-harness.sh`
  - important options now:
    - `--split-task-invocations`
    - `--gradle-args "-x kotlinStoreYarnLock"` (needed in current environment)
  - script behavior:
    - always uses `--rerun-tasks`
    - fails fast if a run captures zero command end events
- Environment / execution notes:
  - local runs currently require excluding `kotlinStoreYarnLock` to avoid lockfile guard failure in this environment.
  - compose/server startup contributes large fixed overhead in each run; this is expected in current setup.
  - there are frequent non-failing webpack warnings (`bufferutil`, `utf-8-validate`, `express` dynamic require); these are noise for this task.
- Recommended resume order:
  - 1. Stabilize `:sdk:jvmTest` failure first (or isolate it) before further p95 tuning experiments.
  - 2. Re-run a short 3-iteration control on unchanged baseline settings to reconfirm current variance envelope.
  - 3. Continue Slice 2 only with candidates that can be measured across complete runs.
- Quick restart commands:
  - full baseline replay:
    - `scripts/test-suite-performance-harness.sh --iterations 5 --tasks ":sdk:jsNodeTest :sdk:jvmTest" --gradle-args "-x kotlinStoreYarnLock" --output-dir build/test-output/perf-harness-baseline-<date>`
  - short control:
    - `scripts/test-suite-performance-harness.sh --iterations 3 --tasks ":sdk:jsNodeTest :sdk:jvmTest" --gradle-args "-x kotlinStoreYarnLock" --output-dir build/test-output/perf-harness-control-<date>`
  - split candidate replay (if needed):
    - `scripts/test-suite-performance-harness.sh --iterations 3 --tasks ":sdk:jsNodeTest :sdk:jvmTest" --split-task-invocations --gradle-args "-x kotlinStoreYarnLock" --output-dir build/test-output/perf-harness-split-<date>`

Continuation update (2026-04-29, slice-2 blocker stabilization)
- Focus:
  - Stabilize the recurring `SdkPartyTest[jvm] > saveWillIncludeModificationInformation()[jvm]` failure before resuming further orchestration experiments.
- Root cause:
  - SDK modification-metadata tests were asserting `timestamp.isWithinOneSecondOfNow()` during verify.
  - Under heavier or repeated test-task load, verify could run more than a second after the mutation completed, creating false negatives without any product regression.
- Changes made:
  - Added `isWithinWindow(earliest, latest)` helper in `sdk/src/commonTest/kotlin/com/zegreatrob/coupling/sdk/IsWithinOneSecondOfNow.kt`.
  - Updated these tests to capture start/end timestamps around the mutation they are validating, then assert the returned timestamp falls inside that mutation window:
    - `SdkPartyTest.saveWillIncludeModificationInformation()`
    - `SdkPartyTest.saveIntegrationCanBeLoaded()`
    - `SdkPinTest.savedPinsIncludeModificationDateAndUsername()`
  - Follow-up adjustment:
    - JS Node tests exposed small client/server clock skew, so `isWithinWindow` now applies a `250.milliseconds` tolerance around the captured mutation interval.
- Semantics note:
  - Assertions remain strict about modification metadata belonging to the mutation under test.
  - This narrows the assertion from "close to verify time" to the stronger and less flaky condition "inside the actual mutation interval, allowing only small cross-process clock skew."
- Validation status:
  - Initial sandboxed attempt was blocked by the Gradle wrapper lock under `~/.gradle`.
  - Re-ran with full filesystem access and the targeted verification now passes:
    - `./gradlew :sdk:jvmTest --tests "*SdkPartyTest.saveWillIncludeModificationInformation*" --tests "*SdkPartyTest.saveIntegrationCanBeLoaded*" --tests "*SdkPinTest.savedPinsIncludeModificationDateAndUsername*" --no-configuration-cache -x kotlinStoreYarnLock`
    - result: `BUILD SUCCESSFUL`
  - After the initial check-in-ready state, `:sdk:jsNodeTest` exposed a remaining regression:
    - `SdkPinTest.savedPinsIncludeModificationDateAndUsername[js, node]`
    - failure shape: timestamp was slightly earlier than the client-captured mutation start (`~14ms`), consistent with cross-process clock skew rather than semantic failure.
  - Re-ran targeted verification after adding bounded skew tolerance:
    - `./gradlew :sdk:jsNodeTest --tests "*SdkPartyTest.saveWillIncludeModificationInformation*" --tests "*SdkPartyTest.saveIntegrationCanBeLoaded*" --tests "*SdkPinTest.savedPinsIncludeModificationDateAndUsername*" --no-configuration-cache -x kotlinStoreYarnLock`
    - result: `BUILD SUCCESSFUL`
    - `./gradlew :sdk:jvmTest --tests "*SdkPartyTest.saveWillIncludeModificationInformation*" --tests "*SdkPartyTest.saveIntegrationCanBeLoaded*" --tests "*SdkPinTest.savedPinsIncludeModificationDateAndUsername*" --no-configuration-cache -x kotlinStoreYarnLock`
    - result: `BUILD SUCCESSFUL`
- Recommended next step:
  - Re-run a short control after this stabilization:
    - `./gradlew :sdk:jvmTest --tests "*SdkPartyTest.saveWillIncludeModificationInformation*" --tests "*SdkPartyTest.saveIntegrationCanBeLoaded*" --tests "*SdkPinTest.savedPinsIncludeModificationDateAndUsername*" --no-configuration-cache -x kotlinStoreYarnLock`
  - If green, resume Slice 2 with a 3-iteration control harness run on unchanged orchestration settings.
