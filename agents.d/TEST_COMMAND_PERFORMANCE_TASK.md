# Task: Command-level test performance analytics (sdk + e2e)

Goal
- Build reliable command-level performance analytics on top of `build/test-output/test.jsonl`.
- Focus first on likely bottlenecks: `:sdk:jvmTest`, `:sdk:jsNodeTest`, and `:e2e:e2eRun`.
- Confirm command logs are parseable and expose actionable slow/fast command metrics and test-suite impact.
- Long-term objective: logs must be directly queryable by standard tools (Splunk, `jq`, etc.) without requiring custom parsers.

Scope
- Use Kotlin-first tooling in `libraries:test-log-analysis` + `analyzeTestJsonl`.
- Keep validator/analyzer strict posture unchanged for schema/lifecycle guarantees.
- Extend analytics output only (no schema regression in existing core event fields).
- Prefer contract-first log quality improvements over downstream parser complexity.

Principle
- Canonical log events should carry structured command fields at top-level/`properties` (for example `properties.command_action`, `properties.command_phase`, `properties.command_trace_id`, `properties.command_duration_ms`) so common log tools can query them directly.
- Custom analyzer parsing of freeform message text is migration-only fallback and should be removed once canonical emission is complete.

Baseline (2026-04-24)
- Confirmed command logging presence in sdk/e2e slices of `test.jsonl`:
  - logger counts across `:sdk:*` + `:e2e:*`:
    - `forwarded-output`: 9444
    - `testmints`: 1576
    - `ktor`: 1246
    - `ActionLogger`: 1204
    - `test-events`: 450
    - `browser`: 309
- Command event shape confirmation:
  - JS sdk logs include canonical `logger=ActionLogger` with parseable payload:
    - `{action=..., type=Start|End, duration=...ms?, traceId=...}`
  - JVM sdk logs include same payload embedded inside forwarded lines:
    - `... INFO ActionLogger - {action=..., type=Start|End, ...}`
- e2e currently emits browser logs, but no ActionLogger command events in current broad run.

Implemented in this slice (temporary bridge)
- Extended analyzer command in `libraries:test-log-analysis`:
  - Parses command logs from both:
    - direct `logger=ActionLogger` message payloads
    - JVM forwarded lines containing `ActionLogger - {action=...}`
  - Emits new metrics in analyze report:
    - `command_log_events_total`
    - `command_log_events_parsed`
    - `command_start_events`
    - `command_end_events`
    - `command_end_events_with_duration`
    - `command_unique_actions`
    - `command_events_by_task`
    - `command_parse_failures_by_task`
    - `command_duration_ms_by_action` (count/total/avg/p50/p95/max)
    - `slowest_command_actions`
    - `tests_with_command_timings`
    - `tests_command_time_share_p50`
    - `tests_command_time_share_p95`
- Added/updated tests:
  - `AnalyzeCommandParityTest` now verifies command parsing + duration aggregation from sdk-style log lines.

Verification (2026-04-24)
- `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --no-configuration-cache` => success.
- `./gradlew analyzeTestJsonl --no-configuration-cache` => success, strict mode, `total_violations=0`.
- Fresh analyzer snapshot (current head):
  - `command_log_events_total=2408`
  - `command_log_events_parsed=2408`
  - `command_events_by_task={ :sdk:jsNodeTest:1204, :sdk:jvmTest:1204 }`
  - `command_parse_failures_by_task={}`
  - `command_unique_actions=20`
  - `slowest_command_actions` top includes:
    - `DeletePartyCommand` (`p95_ms=878.967`, `max_ms=948.803`)
    - `ApplyBoostCommand` (`avg_ms=339.263`, `max_ms=650.406`)
  - `tests_with_command_timings=218`
  - `tests_command_time_share_p50=0.979`
  - `tests_command_time_share_p95=2.137`

Interpretation notes
- Command logs are parseable for sdk JS + JVM and now machine-aggregated in analyzer output.
- `tests_command_time_share_*` can exceed `1.0` when overlapping/nested command timings are summed, which is expected under concurrent command execution.
- e2e currently contributes no ActionLogger command events in this snapshot; only browser logs are present.
- Current command parsing from message text is intentionally transitional and does not satisfy long-term queryability goals by itself.

Next slices
- [x] Slice 1 - canonical command schema + emission
  - Define and document canonical command fields in log events.
  - Update emitters (`ActionLoggingSyntax`/related logging paths) to write structured command fields, not only message text.
- [x] Slice 2 - listener normalization (jvm/js/e2e)
  - Ensure `JsonLoggingTestListener` normalizes command logs into canonical structured fields across platforms.
  - Add e2e command instrumentation path if commands are expected in e2e runs.
- [x] Slice 3 - strict contract enforcement
  - Add analyzer/validator checks for command field presence/typing when command logs exist.
  - Keep message parsing fallback temporarily; track and drive fallback usage to zero.
- [x] Slice 4 - remove fallback parsing + external-query examples
  - Remove message-text command parsing after migration completion.
  - Add `jq`/Splunk query examples and CI artifacts proving no custom tooling is required.
- [ ] Slice 5 - bottleneck report ergonomics
  - Add per-task/per-platform slow-command rollups and top tests by command-time share.

Continuation update (2026-04-24, slice-1 canonical command schema + emission)
- Implemented canonical command emission in action logging:
  - `libraries/action/.../ActionLoggingSyntax.kt` now emits:
    - `command_action`
    - `command_phase` (`start`/`end`)
    - `command_trace_id`
    - `command_duration_ms` (end events)
  - Legacy keys (`action`, `type`, `traceId`, `duration`) are retained during migration.
- Implemented listener-side command normalization:
  - `coupling-plugins/.../JsonLoggingTestListener.kt`
  - JVM forwarded command lines are normalized to:
    - `logger=command`
    - `properties.command=true`
    - canonical `command_*` fields
- Analyzer now supports canonical structured command fields directly (no message parsing required for this path):
  - `libraries/test-log-analysis/.../TestLogTools.kt`
- Added analyzer parity test for canonical field ingestion:
  - `AnalyzeCommandParityTest` (`analyze prefers canonical command properties without message parsing`)
- Verification:
  - `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --no-configuration-cache` => success.
  - `./gradlew :libraries:test-log-analysis:jvmTest --no-configuration-cache` => success.
  - `./gradlew :libraries:action:compileKotlinJvm --no-configuration-cache` => success.
  - `./gradlew -Pcoupling.testLog.reset=true :sdk:jvmTest --no-configuration-cache` => success.
  - `jq` verification on `build/test-output/test.jsonl`:
    - sdk jvm loggers include `command` (`1204` events).
    - command events include `properties.command_action`/`command_phase` and end events include `command_duration_ms`.
  - `./gradlew analyzeTestJsonl --no-configuration-cache` => success, strict mode.
    - snapshot: `command_log_events_total=1204`, `command_log_events_parsed=1204`, `command_parse_failures_by_task={}`.

Continuation status
- checkpoint: `85689e856`
- next: `NEXT=SLICE_3_STRICT_CONTRACT_ENFORCEMENT`
- verify: `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --no-configuration-cache` ; `./gradlew :e2e:compileE2eTestKotlinJs --no-configuration-cache` ; `./gradlew resetTestJsonl --no-configuration-cache` ; `./gradlew :e2e:e2eRun --rerun-tasks --no-configuration-cache` ; `./gradlew analyzeTestJsonl --no-configuration-cache`
- tests: `AnalyzeCommandParityTest analyze prefers canonical command properties without message parsing`

Continuation update (2026-04-24, slice-2 listener normalization js/e2e)
- Implemented E2E command normalization at the E2E log ingress:
  - `e2e/src/jsE2eTest/kotlin/com/zegreatrob/coupling/e2e/test/CheckLogs.kt`
  - Added robust embedded JSON extraction from browser log lines.
  - Added command normalization parity with listener behavior:
    - emits `logger=command`
    - emits `properties.command=true`
    - emits canonical `command_action`, `command_phase`, `command_trace_id`, `command_duration_ms`
- Added non-command ActionLogger handling in E2E forwarding:
  - ActionLogger records without parseable command fields are mapped to `logger=forwarded-output`.
  - Original source logger is preserved as `properties.forwarded_logger=ActionLogger`.
- Clean E2E-only verification:
  - `./gradlew resetTestJsonl --no-configuration-cache` => `build/test-output/test.jsonl` cleared to 0 lines.
  - `./gradlew :e2e:e2eRun --rerun-tasks --no-configuration-cache` => success.
  - `./gradlew analyzeTestJsonl --no-configuration-cache` => success, strict mode, `total_violations=0`.
  - `jq` verification on `build/test-output/test.jsonl` for `:e2e:e2eRun`:
    - logger counts: `command=234`, `browser=71`, `forwarded-output=3`.
    - `command_parse_failures_by_task={}`.
    - no `logger=ActionLogger` entries.
  - analyzer snapshot (clean e2e-only run):
    - `command_log_events_total=234`
    - `command_log_events_parsed=234`
    - `command_events_by_task={ :e2e:e2eRun:234 }`
    - `command_unique_actions=9`

Continuation update (2026-04-24, slice-3 strict contract enforcement)
- Added strict command contract checks in Kotlin test-log tooling (`libraries:test-log-analysis`):
  - Validate path (`validate`):
    - `command_missing_canonical_fields`
    - `command_bad_phase`
    - `command_bad_duration_ms`
    - Included in `total_violations`/`failing_violations` behavior.
  - Analyze path (`analyze`):
    - Enforces canonical command contract for canonical command events (`logger=command` or `properties.command*`), adding strict violations for:
      - missing `command_action` / `command_phase` / `command_trace_id`
      - non-`start|end` `command_phase`
      - non-numeric `command_duration_ms` when present
    - Added migration telemetry to drive fallback usage to zero:
      - `command_canonical_events_total`
      - `command_events_using_message_fallback`
      - `command_message_fallback_by_task`
      - `command_contract_violations`
      - `command_contract_violations_by_task`
- Kept fallback parser in place (transitional):
  - Legacy `ActionLogger`/forwarded message parsing still supported.
  - Fallback usage is now explicitly counted instead of hidden.
- Tests added/updated:
  - `AnalyzeCommandParityTest`
    - canonical contract strict failure case
    - fallback usage telemetry case
    - assertions for canonical/fallback counters
  - `ValidateCommandParityTest`
    - strict validator contract case for malformed canonical command log event
- Verification:
  - `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --tests "*ValidateCommandParityTest*" --no-configuration-cache` => success.
  - `./gradlew analyzeTestJsonl --no-configuration-cache` => success, strict mode.
  - Analyzer snapshot after strict re-run:
    - `command_log_events_total=2642`
    - `command_canonical_events_total=2642`
    - `command_events_using_message_fallback=0`
    - `command_contract_violations=0`
    - `command_events_by_task={ :e2e:e2eRun:234, :sdk:jsNodeTest:1204, :sdk:jvmTest:1204 }`

Continuation status
- checkpoint: working tree (uncommitted)
- next: `NEXT=SLICE_4_REMOVE_FALLBACK_PARSING_AND_ADD_EXTERNAL_QUERY_EXAMPLES`
- verify: `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --tests "*ValidateCommandParityTest*" --no-configuration-cache` ; `./gradlew analyzeTestJsonl --no-configuration-cache`

Continuation update (2026-04-24, slice-4 remove fallback parsing + external query examples)
- Removed analyzer message-text fallback parsing from Kotlin tooling (`libraries:test-log-analysis`):
  - command ingestion now accepts canonical structured fields only (`logger=command`/`properties.command_*`).
  - removed fallback-related analyzer metrics:
    - `command_events_using_message_fallback`
    - `command_message_fallback_by_task`
  - legacy `ActionLogger` message payload lines are no longer counted as command events by analyzer.
- Updated tests:
  - `AnalyzeCommandParityTest`
    - converted sdk-style command timing case to canonical `logger=command` events.
    - added canonical-only guard: `analyze ignores legacy action logger message payloads`.
    - removed fallback telemetry migration test.
- Verification:
  - `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --tests "*ValidateCommandParityTest*" --no-configuration-cache` => success.
  - `./gradlew analyzeTestJsonl --no-configuration-cache` => success, strict mode, `total_violations=0`.
  - analyzer snapshot after fallback removal:
    - `command_log_events_total=5284`
    - `command_canonical_events_total=5284`
    - `command_log_events_parsed=5284`
    - `command_parse_failures_by_task={}`
    - `command_contract_violations=0`

External query examples (no custom parser required)
- `jq`: command event counts by task directly from raw `test.jsonl`
  - `jq -r 'select(.type=="Log" and .logger=="command") | .task' build/test-output/test.jsonl | sort | uniq -c | sort -nr`
- `jq`: direct p95 by action from canonical command end events
  - `jq -s '
      map(select(.type=="Log" and .logger=="command" and .properties.command_phase=="end" and (.properties.command_duration_ms|type)=="number")) |
      group_by(.properties.command_action) |
      map({action: .[0].properties.command_action, p95_ms: (map(.properties.command_duration_ms)|sort|.[((length-1)*0.95|floor)])}) |
      sort_by(.p95_ms) | reverse
    ' build/test-output/test.jsonl`
- Splunk (example SPL using canonical fields in indexed JSON):
  - `index=coupling sourcetype=test_jsonl type=Log logger=command properties.command_phase=end | stats count as events avg(properties.command_duration_ms) as avg_ms perc95(properties.command_duration_ms) as p95_ms max(properties.command_duration_ms) as max_ms by properties.command_action, task | sort - p95_ms`

Continuation status
- checkpoint: working tree (uncommitted)
- next: `NEXT=SLICE_5_BOTTLENECK_REPORT_ERGONOMICS`
- verify: `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --tests "*ValidateCommandParityTest*" --no-configuration-cache` ; `./gradlew analyzeTestJsonl --no-configuration-cache`

Continuation update (2026-04-24, slice-5 bottleneck report ergonomics)
- Added command bottleneck rollups in analyzer output (`libraries:test-log-analysis`):
  - `slowest_command_actions_by_task` (top 5 actions by max duration per task)
  - `slowest_command_actions_by_platform` (top 5 actions by max duration per platform)
- Added per-test bottleneck ranking:
  - `top_tests_by_command_time_share` (top 10 tests by `sum(command_duration_ms)/test duration`)
  - each entry includes `run_id`, `task`, `suite`, `test`, `share`, `command_duration_ms`, `test_duration_ms`.
- Updated tests:
  - `AnalyzeCommandParityTest`
    - extended canonical metrics case with assertions for new task/platform rollups and top-test share output.
    - added `analyze emits per-task per-platform rollups and top test shares`.
- Verification:
  - `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --tests "*ValidateCommandParityTest*" --no-configuration-cache` => success.
  - `./gradlew analyzeTestJsonl --no-configuration-cache` => success, strict mode, `total_violations=0`.
  - analyzer snapshot (same run, highlighting new ergonomics fields):
    - `slowest_command_actions_by_task` includes `:e2e:e2eRun`, `:sdk:jsNodeTest`, `:sdk:jvmTest`.
    - `slowest_command_actions_by_platform` includes `e2e`, `js`, `jvm`.
    - `top_tests_by_command_time_share` populated (10 entries), top examples:
      - `:sdk:jsNodeTest jsNodeTest.com.zegreatrob.coupling.sdk.SdkPartyTest.deleteWillMakePartyInaccessible` (`share=10.767`)
      - `:sdk:jvmTest com.zegreatrob.coupling.sdk.RequestCombineEndpointTest.postPlayersAndPinsThenGet()` (`share=9.131`)

Continuation status
- checkpoint: working tree (uncommitted)
- next: `NEXT=COMPLETE`
- verify: `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --tests "*ValidateCommandParityTest*" --no-configuration-cache` ; `./gradlew analyzeTestJsonl --no-configuration-cache`
