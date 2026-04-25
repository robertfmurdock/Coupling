# Task: Command-to-test attribution clarity in `test.jsonl`

Goal
- Make command log events in `build/test-output/test.jsonl` unambiguously attributable to a single test instance.
- Eliminate timestamp-window inference for per-test command analytics.
- Preserve direct queryability with `jq`/Splunk (no custom parser required).

Problem statement
- Current canonical command events (`logger=command`) include task/run/timing/action fields, but no stable test identity.
- In `:sdk:jsNodeTest` and `:sdk:jvmTest`, tests overlap heavily, so commands cannot be reliably assigned to a single test from timestamps alone.

Success criteria
- Every command event emitted during a test has canonical test identity fields:
  - `properties.test_suite`
  - `properties.test_name`
  - `properties.test_id` (stable per test instance, unique within run)
- Analyzer can produce exact (not inferred) per-test command call counts and durations.
- Strict mode fails when command events are missing required test attribution fields (for tasks/platforms where attribution is expected).
- Existing command schema remains backward compatible aside from newly required attribution in strict validation scope.

Non-goals
- No server-side performance tuning changes in this task.
- No changes to command business logic.
- No change to existing `TestStart`/`TestEnd` event semantics beyond adding/using identifiers as needed.

Canonical attribution contract (proposed)
- Applies to `type=Log`, `logger=command` events generated during test execution.
- Required fields:
  - `properties.test_suite` (string)
  - `properties.test_name` (string)
  - `properties.test_id` (string UUID or equivalent stable opaque id)
- Strongly recommended:
  - `properties.test_task` (echo of `.task` for query ergonomics)
  - `properties.test_platform` (echo of `.platform`)
- Existing required command fields remain:
  - `command_action`, `command_phase`, `command_trace_id`, and `command_duration_ms` (end events)

Rollout slices

- [ ] Slice 1 - test identity source of truth
  - Ensure `TestStart` events include (or derive) a stable `test_id`.
  - Define identity key format and lifecycle:
    - unique per `(run_id, task, suite, test, occurrence)`
    - valid for entire test execution window
  - Document invariants in test logging docs/comments.

- [ ] Slice 2 - context propagation in test runtime
  - Introduce per-test context carrier in test harness/listener layer.
  - Propagate test identity across async/coroutine boundaries used by SDK/e2e tests.
  - Ensure context is set on test start and cleared on test end to avoid leakage.

- [ ] Slice 3 - command emission tagging
  - Update command log emission path(s) to include `test_suite`, `test_name`, `test_id` when context exists.
  - Keep emission no-op outside test context (or mark as non-test command event explicitly if needed).
  - Verify both direct and forwarded/normalized command paths retain tags.

- [ ] Slice 4 - normalization parity (jvm/js/e2e)
  - Ensure `JsonLoggingTestListener` normalization preserves/injects attribution fields for JVM/JS forwarded logs.
  - Ensure e2e log ingestion (`CheckLogs.kt`) maps canonical attribution fields from browser payload into top-level command properties.
  - Confirm no `logger=command` event is emitted without attribution in expected tasks.

- [ ] Slice 5 - strict validation and analysis upgrade
  - Add strict analyzer/validator checks:
    - `command_missing_test_attribution_fields`
    - optional type/format checks for `test_id`
  - Update bottleneck metrics to use exact test attribution joins (no timestamp overlap fallback).
  - Keep migration toggle if needed, defaulting to strict in CI once green.

- [ ] Slice 6 - query examples and CI guardrails
  - Add `jq` examples for exact per-test command counts and top tests by command time:
    - group by `test_id`/`test_suite`/`test_name`
  - Add Splunk query examples using `properties.test_*`.
  - Add CI check/report assertion that attributed command ratio is 100% for targeted tasks.

Verification commands (incremental)
- `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --tests "*ValidateCommandParityTest*" --no-configuration-cache`
- `./gradlew -Pcoupling.testLog.reset=true :sdk:jvmTest --no-configuration-cache`
- `./gradlew -Pcoupling.testLog.reset=true :sdk:jsNodeTest --no-configuration-cache`
- `./gradlew resetTestJsonl --no-configuration-cache`
- `./gradlew :e2e:e2eRun --rerun-tasks --no-configuration-cache`
- `./gradlew analyzeTestJsonl --no-configuration-cache`

Suggested jq acceptance checks
- 100% of command events in target tasks include test attribution:
  - `jq -r 'select(.type=="Log" and .logger=="command" and (.task==":sdk:jvmTest" or .task==":sdk:jsNodeTest" or .task==":e2e:e2eRun")) | select((.properties.test_id|type)!="string" or (.properties.test_suite|type)!="string" or (.properties.test_name|type)!="string") | .task' build/test-output/test.jsonl | wc -l`
  - expected `0`
- Distinct command events groupable exactly by `test_id`:
  - `jq -r 'select(.type=="Log" and .logger=="command" and .properties.test_id!=null) | .properties.test_id' build/test-output/test.jsonl | wc -l`

Risks / pitfalls
- Async context loss may cause intermittent missing attribution tags.
- Context leakage across tests can produce incorrect attribution if not cleared deterministically.
- e2e/browser payload shape drift may require defensive extraction.
- Retries/parameterized tests may require occurrence-aware `test_id` generation.

Definition of done
- Analyzer strict report shows zero attribution violations.
- Exact per-test command metrics are computed from command event tags, not timestamp overlap.
- Query examples run directly on raw JSONL and produce stable results across repeated runs.

Continuation status
- checkpoint: working tree (uncommitted)
- next: `NEXT=SLICE_1_TEST_IDENTITY_SOURCE_OF_TRUTH`
