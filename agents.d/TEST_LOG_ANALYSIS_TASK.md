# Task: Normalize test.jsonl for performance analysis

Goal
- Make `build/test-output/test.jsonl` fully machine-readable and consistent across JVM + JS test suites.
- Enable reliable automated performance analysis (durations, pass/fail rates, slow tests, per-suite metrics).

Current State (2026-04-21)
- Validation is now wired to Gradle via `validateTestJsonl` and runs from root `check`.
- Validator has two modes:
  - `compat` (default): report-only, does not fail build.
  - `strict` (`--strict`): fails on any schema violation.
- JS console hook now emits JSON `Log` records with `type`, `platform=js`, `run_id`, `task`, `timestamp`.
- JS task start/finish markers are emitted as structured JSON.
- Full-suite output still contains legacy/unnormalized records:
  - Log4j events with event payload nested under `message`.
  - Missing top-level `type`, `run_id`, `platform` on many JVM/forwarded entries.
  - Some non-JSON remnants in full mixed runs.

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

2) Normalize JVM listener output (highest impact)
   - Update `JsonLoggingTestListener` to emit top-level schema keys (`type`, `task`, `suite`, `test`, `run_id`, `platform=jvm`, `timestamp`).
   - Convert `TestEnd.duration` to numeric `duration_ms`.
   - Keep legacy payload under `properties.legacy` temporarily when needed.

3) Remove remaining non-JSON ingress
   - Ensure forwarded test output lines are wrapped as `type=Log` JSON events only.
   - Eliminate raw TeamCity/plain text append paths to `test.jsonl`.

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
