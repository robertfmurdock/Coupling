# Task: Normalize test.jsonl for performance analysis

Goal
- Make `build/test-output/test.jsonl` fully machine-readable and consistent across JVM + JS test suites.
- Enable reliable automated performance analysis (durations, pass/fail rates, slow tests, per-suite metrics).

Current State (2026-04-21, late-night snapshot)
- Validation is wired to Gradle via `validateTestJsonl` and runs from root `check`.
- Validator modes are in place:
  - `compat` (default): report-only, does not fail build.
  - `strict` (`--strict`): fails on any schema violation.
- Validator is contract-first again (no legacy-shape inference/parsing heuristics).
- JS emitters now write structured `Log` events (`type`, `platform=js`, `run_id`, `task`, `timestamp`).
- JVM `JsonLoggingTestListener` now emits canonical top-level events directly:
  - `TestStart`/`TestEnd` with top-level schema fields.
  - Numeric `duration_ms` on `TestEnd`.
  - Forwarded stdout/stderr mapped to `type=Log`.
- `resetTestJsonl` is opt-in only:
  - `-Pcoupling.testLog.reset=true` or `COUPLING_TEST_LOG_RESET=true`.
  - Default behavior remains append (supports multi-run/week-long analysis).
- Root cause found for broad-run JVM violations:
  - Test Log4j config was writing raw Log4j JSON directly to `test.jsonl` (bypassing canonical listener fields).
  - This produced `missing core: type,run_id,platform` violations on fresh JVM runs.
- Fix applied:
  - `WriteTestLog4j2Config` now routes root logger to `Console` (stdout) instead of `File(test.jsonl)`.
  - Listener normalization (`JsonLoggingTestListener.onOutput`) now remains the single ingress path for JVM log lines.
- Fresh strict checks after fix:
  - `./gradlew -Pcoupling.testLog.reset=true :sdk:jvmTest :sdk:jsNodeTest :client:components:jsNodeTest`
  - `./gradlew -Pcoupling.testLog.reset=true --rerun-tasks :sdk:jvmTest :libraries:action:jvmTest`
  - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl` => **0 violations**.
- Continuation update (2026-04-21, later):
  - Found remaining JS non-canonical ingress:
    - `libraries/logging/src/jsMain/kotlin/.../JsonFormatter.kt` appended raw logger JSON directly to `test.jsonl`.
    - This produced lines missing canonical fields (`type/run_id/platform`) and contributed to parse instability.
  - Fix applied:
    - Removed direct `test.jsonl` writes from JS `JsonFormatter`; logging now relies on canonical test-log emitters.
  - Found remaining JS parse issue:
    - Oversized `##teamcity[...]` console payload lines could interleave at append time and create `non-json` fragments.
  - Fix applied:
    - `WriteJsTestLogHook` now ignores TeamCity control lines (`##teamcity[...]`).
    - Hook also truncates oversized console messages (12k chars) before append.
  - Fresh strict checks after continuation fixes:
    - `./gradlew -Pcoupling.testLog.reset=true --rerun-tasks :sdk:jsNodeTest :client:components:jsNodeTest`
    - `./gradlew --rerun-tasks :sdk:jvmTest`
    - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl` => **0 violations** (`non_json_lines=0`, `missing_core_fields=0`).
- Continuation update (2026-04-21, broad-sweep follow-up):
  - Broad strict run surfaced residual `non-json` lines in `:libraries:repository:dynamo:jsNodeTest` only:
    - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl` on wide run showed `non_json_lines=4`.
  - Root cause:
    - Two concurrent JS writers appended to the same `test.jsonl` file:
      - Gradle-side `JsonLoggingTestListener` (canonical output listener).
      - Node-side `WriteJsTestLogHook` (via `NODE_OPTIONS=--require ...`).
    - Under very large `testmints` payload logs, writes interleaved and split JSON objects across lines.
  - Fix applied:
    - Removed JS hook injection from `KotlinJsTest` configuration in `coupling-plugins/.../testLogging.gradle.kts`.
    - JS logging now uses a single ingress path (`JsonLoggingTestListener`) for `test.jsonl`.
  - Post-fix strict verification:
    - `./gradlew -Pcoupling.testLog.reset=true --rerun-tasks :libraries:repository:dynamo:jsNodeTest`
    - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl` => **0 violations**.
    - Broad sweep (excluding failing unrelated `:konsist:jvmTest`):
      - `./gradlew -Pcoupling.testLog.reset=true --rerun-tasks $(./gradlew tasks --all | awk '/:[^ ]+:(jvmTest|jsNodeTest) -/{print ":"$1}' | grep -v '^:konsist:jvmTest$')`
      - strict validator result: **0 violations**, `non_json_lines=0`, `missing_core_fields=0`.
- Continuation update (2026-04-22, phase-A tightening):
  - Implemented progressive validator tightening for default `check` path:
    - Added validator flag `--fail-on-non-json` in `scripts/validate-test-jsonl.mjs`.
    - Non-strict mode now supports `mode=compat-fail-non-json` where only `non_json_lines` are build-failing; all schema violations remain reported.
  - Adjusted rollout to opt-in (same day hotfix):
    - Full-suite `check` still contains non-canonical browser/e2e console lines, so default fail-on-non-json was too aggressive.
    - Gradle root `validateTestJsonl` now enables phase-A mode only with `-Pcoupling.testLog.failNonJson=true`.
    - Default behavior remains compat report-only until remaining ingress cleanup is complete.
  - Verification:
    - `./gradlew validateTestJsonl` (default) reports `mode=compat`.
    - `./gradlew -Pcoupling.testLog.failNonJson=true validateTestJsonl` reports `mode=compat-fail-non-json`.
- Continuation update (2026-04-22, e2e canonicalization):
  - Root cause for residual broad-run `non-json` lines was e2e browser-log forwarding:
    - `e2e/src/jsE2eTest/kotlin/com/zegreatrob/coupling/e2e/test/CheckLogs.kt` appended raw browser console strings directly to `test.jsonl`.
  - Fix applied:
    - Replaced raw append path with canonical event writes:
      - `type=Log`, `platform=e2e`, `run_id`, `task`, `timestamp`, `logger`, `message`, optional `properties`.
    - Wired e2e task metadata into runtime env:
      - `COUPLING_TEST_RUN_ID` and `COUPLING_TEST_TASK` are now passed from `:e2e:e2eRun`.
  - Verification:
    - `./gradlew :e2e:compileE2eTestKotlinJs` (build validation) => success.
    - `./gradlew -Pcoupling.testLog.reset=true --rerun-tasks :e2e:e2eRun`
    - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl` => **0 violations** (`parsed_json_lines=305`, `non_json_lines=0`).
- Continuation update (2026-04-22, broad JVM+JS re-check):
  - Re-ran broad strict verification after phase-A and e2e canonicalization changes:
    - `./gradlew -Pcoupling.testLog.reset=true --rerun-tasks $(./gradlew tasks --all | awk '/:[^ ]+:(jvmTest|jsNodeTest) -/{print ":"$1}' | grep -v '^:konsist:jvmTest$')`
    - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl`
  - Result: **0 violations** with no parse or schema issues:
    - `non_json_lines=0`
    - `missing_core_fields=0`
    - `missing_end_fields=0`
    - `bad_duration_ms=0`
  - Snapshot from strict report:
    - `parsed_json_lines=4928`
    - `type_counts={ Log: 4016, TestStart: 456, TestEnd: 456 }`
    - `platform_counts={ js: 4590, jvm: 338 }`
- Continuation update (2026-04-22, phase-A default-on):
  - Re-validated broad JVM+JS sweep and strict schema status:
    - `./gradlew -Pcoupling.testLog.reset=true --rerun-tasks $(./gradlew tasks --all | awk '/:[^ ]+:(jvmTest|jsNodeTest|e2eRun) -/{print ":"$1}' | grep -v '^:konsist:jvmTest$')`
    - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl`
    - strict result: **0 violations**.
  - Re-ran e2e flow and strict validation to ensure `platform=e2e` lines stay canonical:
    - `./gradlew --rerun-tasks :e2e:e2eRun`
    - strict report snapshot: `parsed_json_lines=5193`, `non_json_lines=0`, `missing_core_fields=0`, `platform_counts={ js: 4546, jvm: 338, e2e: 309 }`.
  - Tightening change applied:
    - Root `validateTestJsonl` now defaults to `--fail-on-non-json` (phase A on by default for `check` finalizer path).
    - Backward-compat override remains available:
      - `-Pcoupling.testLog.failNonJson=false` forces `mode=compat`.
  - Verification:
    - `./gradlew validateTestJsonl` reports `mode=compat-fail-non-json`.
    - `./gradlew -Pcoupling.testLog.failNonJson=false validateTestJsonl` reports `mode=compat`.

Deliverable
- A single, consistent JSON schema for every line in `test.jsonl`.
- Each test event should be a JSON object with required fields:
  - `type`: `TestStart | TestEnd | StepStart | StepEnd | Log`
  - `task`: Gradle task path (e.g. `:libraries:json:jsNodeTest`)
  - `suite`: test class or suite name
  - `test`: test method/name
  - `status`: `SUCCESS | FAILURE | SKIPPED` (for end events)
  - `duration_ms`: number (for end events)
  - `timestamp`: ISO-8601
  - `run_id`: stable run identifier (existing `testRunIdentifier`)
  - `platform`: `jvm | js | e2e`
  - `logger`: original logger name (if applicable)
  - `message`: string (optional)
  - `properties`: map (optional)

Near-Term Plan (delivery-first)
1) Keep CI green while preserving visibility
   - Keep validator in phase-A default mode (`compat-fail-non-json`) for `check`.
   - Retain escape hatch `-Pcoupling.testLog.failNonJson=false` if a temporary unblock is needed.
   - Keep strict mode runnable in CI/nightly (`node scripts/validate-test-jsonl.mjs --strict ...`) for tracking.

2) Broaden strict verification scope
   - Continue strict checks across larger task sets (beyond current targeted JS/JVM slices) to identify any remaining outliers.
   - Capture offending task paths and event types for targeted cleanup.

3) Remove remaining non-canonical ingress
   - Verify no other writers bypass listener normalization.
   - Ensure all producers emit schema-compliant top-level JSON fields.

4) Tighten validator progressively
   - Phase A: complete (default fail on non-JSON lines).
   - Phase B: fail on missing `type/timestamp/run_id/platform`.
   - Phase C: fail full schema by event type (end-event requirements included).

5) Flip default to strict
   - Once full test suite passes strict mode consistently, make strict the default for `check`.

Suggested Implementation Details
- Create a dedicated Kotlin data class for test events and serialize with Jackson.
- In JVM listener, replace `ObjectMessage`/nested message payloads with top-level schema fields.
- For JS, add a test framework hook or reporter that writes JSON directly (avoid TeamCity output).
- Use `run_id` sourced from existing `testRunIdentifier`.
- Ensure the output file is always created even if no tests are executed.

Verification Steps
- Quick pass check:
  - `./gradlew validateTestJsonl` succeeds (`mode=compat-fail-non-json`).
  - `./gradlew -Pcoupling.testLog.failNonJson=false validateTestJsonl` succeeds (`mode=compat`).
- Strict progress check:
  - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl` reports zero violations on broad JVM/JS/e2e runs.
- Cutover check:
  - Full-suite strict run has zero violations before switching `check` to strict mode.
