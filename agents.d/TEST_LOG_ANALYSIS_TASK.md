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
   - Keep validator in `compat` mode as default for `check`.
   - Keep strict mode runnable in CI/nightly (`node scripts/validate-test-jsonl.mjs --strict ...`) for tracking.

2) Broaden strict verification scope
   - Continue strict checks across larger task sets (beyond current targeted JS/JVM slices) to identify any remaining outliers.
   - Capture offending task paths and event types for targeted cleanup.

3) Remove remaining non-canonical ingress
   - Verify no other writers bypass listener normalization.
   - Ensure all producers emit schema-compliant top-level JSON fields.

4) Tighten validator progressively
   - Phase A: fail only on non-JSON lines in default mode.
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
  - `./gradlew validateTestJsonl` succeeds (compat mode).
- Strict progress check:
  - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl` reports declining violations over time.
- Cutover check:
  - Full-suite strict run has zero violations before switching `check` to strict mode.
