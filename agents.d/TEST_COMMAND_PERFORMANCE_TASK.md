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
- [ ] Slice 2 - listener normalization (jvm/js/e2e)
  - Ensure `JsonLoggingTestListener` normalizes command logs into canonical structured fields across platforms.
  - Add e2e command instrumentation path if commands are expected in e2e runs.
- [ ] Slice 3 - strict contract enforcement
  - Add analyzer/validator checks for command field presence/typing when command logs exist.
  - Keep message parsing fallback temporarily; track and drive fallback usage to zero.
- [ ] Slice 4 - remove fallback parsing + external-query examples
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
- next: `NEXT=SLICE_2_LISTENER_NORMALIZATION_JS_E2E`
- verify: `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --no-configuration-cache` ; `./gradlew -Pcoupling.testLog.reset=true :sdk:jvmTest --no-configuration-cache` ; `./gradlew analyzeTestJsonl --no-configuration-cache`
- tests: `AnalyzeCommandParityTest analyze prefers canonical command properties without message parsing`
