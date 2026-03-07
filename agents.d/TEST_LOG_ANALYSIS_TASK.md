# Task: Normalize test.jsonl for performance analysis

Goal
- Make `build/test-output/test.jsonl` fully machine-readable and consistent across JVM + JS test suites.
- Enable reliable automated performance analysis (durations, pass/fail rates, slow tests, per-suite metrics).

Current Issues
- Mixed log formats: Log4j JSON, TeamCity `testStdOut` text, and plain console strings.
- Missing fields on many lines (no `taskName`, `testName`, `duration_ms`, `status`).
- JS mint logs are wrapped in TeamCity output and not structured JSON.
- Durations are inconsistent: JVM has `duration` as string in TestEnd, JS mint steps are text only.

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

Plan of Work
1) JVM test event normalization
   - Replace Log4j `message` ObjectMessage outputs with a normalized JSON event payload.
   - For `JsonLoggingTestListener`, emit:
     - `TestStart` event with `task`, `suite`, `test`, `timestamp`, `run_id`, `platform=jvm`.
     - `TestEnd` event with `status`, `duration_ms`, and any failure summary.
   - Ensure timestamps are ISO-8601 and `duration_ms` is numeric.
   - Use a single `logger` name, e.g. `test-events`, to make filtering easy.

2) JS test event normalization
   - Replace TeamCity stdout log capture with a structured JSON emitter.
   - Preferred approach: add a JS-side test reporter or hook that emits JSON with the schema above.
     - If using Kotlin/JS test framework hooks, emit on test start/end and for mint steps.
     - If using console output, override console methods in `js-test-log-hook.js` to emit JSON with `type=Log`.
   - Ensure the JS test runner writes to `COUPLING_TEST_LOG_PATH` directly and never emits mixed text.

3) Mint logger integration
   - Map mint “setup/exercise/verify” steps to:
     - `StepStart`/`StepEnd` events with `phase` field (e.g. `setup`, `exercise`, `verify`).
   - Record `duration_ms` for each step if possible.
   - Include `test` and `suite` fields in every mint event.

4) Event filtering + validation
   - Add a small verifier (script or Gradle task) that:
     - Reads `test.jsonl`.
     - Validates every line against the schema.
     - Fails if any line is non-JSON or missing required keys.
   - Add a summary report step (counts, slowest tests) as proof-of-utility.

5) Backward compatibility
   - If old formats must remain, add a `legacy` field that retains the original line.
   - Otherwise, enforce “JSON only” output in `test.jsonl`.

Suggested Implementation Details
- Create a dedicated Kotlin data class for test events and serialize with Jackson.
- In JVM listener, replace `ObjectMessage` with a serialized JSON string.
- For JS, add a test framework hook or reporter that writes JSON directly (avoid TeamCity output).
- Use `run_id` sourced from existing `testRunIdentifier`.
- Ensure the output file is always created even if no tests are executed.

Verification Steps
- Run a JVM test task with known testmints activity and verify:
  - Only JSON lines in `test.jsonl`.
  - `TestStart`/`TestEnd` entries include `duration_ms`.
  - Mint steps map to `StepStart`/`StepEnd`.
- Run a JS test task and verify:
  - JSON lines contain `platform=js` and `task` is the JS task path.
  - No TeamCity `testStdOut` strings appear.
- Run combined tasks and confirm all lines conform to schema.
