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

- [x] Slice 1 - test identity source of truth
  - Ensure `TestStart` events include (or derive) a stable `test_id`.
  - Define identity key format and lifecycle:
    - unique per `(run_id, task, suite, test, occurrence)`
    - valid for entire test execution window
  - Document invariants in test logging docs/comments.

- [x] Slice 2 - context propagation in test runtime
  - Introduce per-test context carrier in test harness/listener layer.
  - Propagate test identity across async/coroutine boundaries used by SDK/e2e tests.
  - Ensure context is set on test start and cleared on test end to avoid leakage.

- [x] Slice 3 - command emission tagging
  - Update command log emission path(s) to include `test_suite`, `test_name`, `test_id` when context exists.
  - Keep emission no-op outside test context (or mark as non-test command event explicitly if needed).
  - Verify both direct and forwarded/normalized command paths retain tags.

- [x] Slice 4 - normalization parity (jvm/js/e2e)
  - Ensure `JsonLoggingTestListener` normalization preserves/injects attribution fields for JVM/JS forwarded logs.
  - Ensure e2e log ingestion (`CheckLogs.kt`) maps canonical attribution fields from browser payload into top-level command properties.
  - Confirm no `logger=command` event is emitted without attribution in expected tasks.

- [x] Slice 5 - strict validation and analysis upgrade
  - Add strict analyzer/validator checks:
    - `command_missing_test_attribution_fields`
    - optional type/format checks for `test_id`
  - Update bottleneck metrics to use exact test attribution joins (no timestamp overlap fallback).
  - Keep migration toggle if needed, defaulting to strict in CI once green.

- [x] Slice 6 - query examples and CI guardrails
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
- Exact per-test command call counts:
  - `jq -r 'select(.type=="Log" and .logger=="command" and .properties.test_id!=null and .properties.command_action!=null) | [.properties.test_id,.properties.test_suite,.properties.test_name,.properties.command_action] | @tsv' build/test-output/test.jsonl | sort | uniq -c | sort -nr`
- Top tests by command time (exact attribution):
  - `jq -r 'select(.type=="Log" and .logger=="command" and .properties.command_phase=="end" and (.properties.command_duration_ms|type)=="number" and .properties.test_id!=null) | [.properties.test_id,.properties.test_suite,.properties.test_name, (.properties.command_duration_ms|tostring)] | @tsv' build/test-output/test.jsonl | awk -F\"\\t\" '{k=$1\"\\t\"$2\"\\t\"$3; s[k]+=$4} END {for (k in s) print s[k]\"\\t\"k}' | sort -nr | head -20`

Suggested Splunk checks
- Attributed command ratio in target tasks:
  - `index=coupling sourcetype=test_jsonl type=Log logger=command (task=":sdk:jvmTest" OR task=":sdk:jsNodeTest" OR task=":e2e:e2eRun") | eval attributed=if(isnotnull(properties.test_id) AND isnotnull(properties.test_suite) AND isnotnull(properties.test_name),1,0) | stats count as total sum(attributed) as attributed_count | eval ratio=round(attributed_count/total,3)`
- Exact top tests by command time:
  - `index=coupling sourcetype=test_jsonl type=Log logger=command properties.command_phase=end (task=":sdk:jvmTest" OR task=":sdk:jsNodeTest" OR task=":e2e:e2eRun") | stats sum(properties.command_duration_ms) as command_ms by properties.test_id, properties.test_suite, properties.test_name | sort - command_ms`

CI guardrail
- New root task: `./gradlew assertCommandAttributionCoverage --no-configuration-cache`
  - Depends on `analyzeTestJsonl` and reads `build/reports/test-logs/analyze-test-jsonl.json`.
  - Fails if:
    - `command_events_missing_any_test_attribution > 0`, or
    - `command_events_in_attribution_scope > 0` and `command_events_with_full_test_attribution_ratio < 1.0`.

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
- next: `NEXT=DONE`

Continuation update (2026-04-25, slice-6 + full targeted verification)
- Added analyzer attribution coverage metrics:
  - `command_events_in_attribution_scope`
  - `command_events_with_full_test_attribution`
  - `command_events_missing_any_test_attribution`
  - `command_events_with_full_test_attribution_ratio`
- Added CI guard task in root build:
  - `assertCommandAttributionCoverage` (depends on `analyzeTestJsonl` report)
  - fails when missing attribution events exist or ratio `< 1.0`.
- Added/updated query examples for exact per-test command grouping and Splunk parity.
- Fixed e2e canonical log identity consistency:
  - e2e `Log` events now include top-level `test_id` (not just `properties.test_id`).
  - blank env-based test ids are normalized away; absent IDs are not written as empty strings.
- Verification:
  - `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --tests "*ValidateCommandParityTest*" --no-configuration-cache` => success.
  - `./gradlew :e2e:compileE2eTestKotlinJs --no-configuration-cache` => success.
  - `./gradlew -Pcoupling.testLog.reset=true resetTestJsonl :e2e:e2eRun --rerun-tasks --no-configuration-cache` => success.
  - `./gradlew :sdk:jvmTest :sdk:jsNodeTest --no-configuration-cache` => success.
  - `./gradlew analyzeTestJsonl assertCommandAttributionCoverage --no-configuration-cache` => success.
    - coverage snapshot: `in_scope=2642 fully_attributed=2642 missing_any=0 ratio=1.0`.
