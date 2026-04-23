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
- Continuation update (2026-04-22, testmints queryability + resume checkpoint):
  - Added structured TestMints normalization in JVM listener output path:
    - `coupling-plugins/src/main/kotlin/com/zegreatrob/coupling/plugins/JsonLoggingTestListener.kt`
    - For Log events recognized as TestMints, listener now emits:
      - `logger=testmints`
      - `properties.testmints=true`
      - `properties.testmints_phase` (e.g. `setup-start`, `verify-finish`)
      - optional `properties.testmints_step`, `properties.testmints_state`, `properties.testmints_name`
    - This makes TestMints phase analysis queryable as JSON fields instead of only message-string parsing.
  - Added dedicated analyzer for coverage + TestMints phase checks:
    - New script: `scripts/analyze-test-jsonl.mjs`
    - New root Gradle task: `analyzeTestJsonl`
      - default: report mode (never fails build)
      - strict: `-Pcoupling.testLog.analyze.strict=true` (fails on detected coverage/TestMints violations)
  - Verification (passing/safe default path):
    - `./gradlew :coupling-plugins:compileKotlin validateTestJsonl analyzeTestJsonl` => **success**
    - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl` => **0 violations**
  - Verification (strict analyzer, expected current failure on gap):
    - `./gradlew -Pcoupling.testLog.analyze.strict=true analyzeTestJsonl`
    - On current `:sdk:jvmTest` snapshot, strict analyzer reports:
      - `tests_missing_expected_testmints=43`
      - `tests_with_testmints=0`
    - This indicates a real TestMints emission gap in that JVM runtime path (likely plugin/runtime behavior), not JSON parser noise.
  - Resume priority:
    - Investigate why `:sdk:jvmTest` suites importing TestMints produce no TestMints Log events in `test.jsonl` while other JVM tasks (e.g. `:libraries:action:jvmTest`) do.
    - Treat as upstream/plugin integration issue if confirmed; do not compensate by weakening analyzer semantics.
- Continuation update (2026-04-22, sdk:jvmTest TestMints phase recovery):
  - Investigation findings:
    - `:sdk:jvmTest` was emitting TestMints phase text (`test-start`, `setup-start`, etc.) as plain forwarded console output.
    - Listener normalization required either `logger=testmints` or explicit `[testmints]` markers, so phase-only messages stayed `logger=forwarded-output`.
    - Strict analyzer therefore reported false-negative coverage (`tests_with_testmints=0`) despite phase lines being present.
  - Fixes applied:
    - `sdk/build.gradle.kts`
      - enabled JUnit Platform + extension autodetection for `jvmTest` (aligns with modules that already emit canonical TestMints lifecycle phases).
      - added `jvmTestImplementation(kotlin("test-junit5"))`.
      - excluded transitive `org.jetbrains.kotlin:kotlin-test-junit` from `:libraries:repository:validation` in `commonTestImplementation` to resolve JUnit4/JUnit5 capability conflict.
    - `coupling-plugins/src/main/kotlin/.../JsonLoggingTestListener.kt`
      - expanded TestMints detection to classify known phase messages as TestMints events even when logger metadata is missing.
      - added camelCase-to-canonical phase mapping (`exerciseStart` -> `exercise-start`, etc.) and preserved canonical phase tagging in `properties.testmints_phase`.
  - Verification:
    - `./gradlew :coupling-plugins:compileKotlin -Pcoupling.testLog.reset=true --rerun-tasks :sdk:jvmTest`
    - `node scripts/analyze-test-jsonl.mjs --strict build/test-output/test.jsonl`
    - strict result: **0 violations** with recovered coverage:
      - `tests_with_testmints=112`
      - `expected_testmints_tests=80`
      - `tests_missing_expected_testmints=0`
      - `tests_missing_required_testmints_phases=0`
      - `phase_counts={ setup-start:112, setup-finish:112, exercise-start:112, exercise-finish:112, verify-start:112, verify-finish:112, test-start:112, test-finish:112 }`
- Continuation update (2026-04-23, JVM convention cleanup + konsist stabilization):
  - Standardization cleanup:
    - Centralized JVM test conventions in `coupling-plugins/.../testLogging.gradle.kts`:
      - `useJUnitPlatform()`
      - `junit.jupiter.extensions.autodetection.enabled=true`
    - Removed duplicated per-module JVM test platform wiring from:
      - `libraries/action/build.gradle.kts`
      - `libraries/test-action/build.gradle.kts`
      - `sdk/build.gradle.kts` (kept only sdk-specific task ordering/dependencies for compose/cert).
  - JUnit backend alignment (remove sdk-local workaround):
    - Root cause for sdk conflict was upstream dependency export from `libraries/repository/validation`.
    - Replaced `kotlin-test-junit` with `kotlin("test-junit5")` in:
      - `libraries/repository/validation/build.gradle.kts`
    - Removed sdk-specific conflict handling:
      - removed transitive exclude of `kotlin-test-junit`
      - removed sdk-local explicit `kotlin("test-junit5")` add
    - Verified dependency resolution:
      - `:sdk:jvmTestRuntimeClasspath` resolves `kotlin-test-junit5` and no `kotlin-test-junit`.
  - Konsist follow-up after convention changes:
    - `:konsist:jvmTest` initially failed with `NoClassDefFoundError` in `Slf4jLoggerFactory` (missing SLF4J backend).
    - Fixed by centralizing test runtime backend in plugin (instead of module-local dependency):
      - Added `org.slf4j:slf4j-simple` to `testRuntimeOnly`/`jvmTestRuntimeOnly` via `configurations.configureEach` in `testLogging.gradle.kts`.
    - Confirmed `:konsist:jvmTest` passes after centralized backend wiring.
  - Verification snapshot:
    - `./gradlew :konsist:jvmTest --rerun-tasks` => **success**
    - `./gradlew -Pcoupling.testLog.reset=true --rerun-tasks :sdk:jvmTest` => **success**
    - `node scripts/analyze-test-jsonl.mjs --strict build/test-output/test.jsonl` => **0 violations**
      - `tests_with_testmints=112`
      - `tests_missing_expected_testmints=0`
      - `tests_missing_required_testmints_phases=0`
- Continuation update (2026-04-23, phase-B default-on):
  - Re-ran broad JVM/JS/e2e strict verification after latest plugin/schema hardening:
    - `./gradlew -Pcoupling.testLog.reset=true --rerun-tasks $(./gradlew tasks --all | awk '/(^|:)[^ ]+:(jvmTest|jsNodeTest|e2eRun) -/{print ":"$1}' | grep -v '^:konsist:jvmTest$')`
    - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl`
    - `node scripts/analyze-test-jsonl.mjs --strict build/test-output/test.jsonl`
  - Result: **0 strict violations** (validator + analyzer), including e2e lines:
    - strict validator snapshot:
      - `parsed_json_lines=19758`
      - `non_json_lines=0`
      - `missing_core_fields=0`
      - `platform_counts={ js: 8176, jvm: 11273, e2e: 309 }`
    - strict analyzer snapshot:
      - `unique_tests=727`
      - `tests_missing_expected_testmints=0`
      - `tests_missing_required_testmints_phases=0`
  - Tightening change applied:
    - Added validator flag `--fail-on-missing-core` (build-failing in compat mode for missing `type/timestamp/run_id/platform`).
    - Root `validateTestJsonl` now enables both by default:
      - `--fail-on-non-json` (phase A)
      - `--fail-on-missing-core` (phase B)
    - Backward-compat overrides remain available:
      - `-Pcoupling.testLog.failNonJson=false`
      - `-Pcoupling.testLog.failMissingCore=false`
  - Effective default mode:
    - `compat-fail-non-json-core`

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
   - Keep validator in phase-B default mode (`compat-fail-non-json-core`) for `check`.
   - Retain escape hatches if a temporary unblock is needed:
     - `-Pcoupling.testLog.failNonJson=false`
     - `-Pcoupling.testLog.failMissingCore=false`
   - Keep strict mode runnable in CI/nightly (`node scripts/validate-test-jsonl.mjs --strict ...`) for tracking.

2) Broaden strict verification scope
   - Continue strict checks across larger task sets (beyond current targeted JS/JVM slices) to identify any remaining outliers.
   - Capture offending task paths and event types for targeted cleanup.

3) Remove remaining non-canonical ingress
   - Verify no other writers bypass listener normalization.
   - Ensure all producers emit schema-compliant top-level JSON fields.

4) Tighten validator progressively
   - Phase A: complete (default fail on non-JSON lines).
   - Phase B: complete (default fail on missing `type/timestamp/run_id/platform`).
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
  - `./gradlew validateTestJsonl` succeeds (`mode=compat-fail-non-json-core`).
  - `./gradlew -Pcoupling.testLog.failMissingCore=false validateTestJsonl` succeeds (`mode=compat-fail-non-json`).
  - `./gradlew -Pcoupling.testLog.failNonJson=false -Pcoupling.testLog.failMissingCore=false validateTestJsonl` succeeds (`mode=compat`).
- Strict progress check:
  - `node scripts/validate-test-jsonl.mjs --strict build/test-output/test.jsonl` reports zero violations on broad JVM/JS/e2e runs.
- Cutover check:
  - Full-suite strict run has zero violations before switching `check` to strict mode.

Continuation update (2026-04-23, Kotlin-first migration plan for log verification)
- Objective shift:
  - Replace Node-based `scripts/validate-test-jsonl.mjs` and `scripts/analyze-test-jsonl.mjs` with Kotlin/JVM tooling while preserving behavior and Gradle UX.
  - Keep migration resumable in ~15-minute commit increments with explicit continuation markers.

Execution Strategy (best path)
- Use `library + thin CLI` from the start:
  - core rules/parsing/report models in a reusable library module (recommended: `libraries:test-log-analysis`)
  - operational entrypoint in a thin Kotlin/JVM CLI module (recommended: `cli:test-log-tools`) with commands:
    - `validate` (parity with current validator flags/modes)
    - `analyze` (parity with current analyzer strict/report behavior)
- Wire root Gradle tasks (`validateTestJsonl`, `analyzeTestJsonl`) to JavaExec/Kotlin main instead of `node ...mjs`.
- During migration, run dual validation (Kotlin + JS) until parity is proven, then remove JS scripts.
- Use a strict TDD loop within each migration slice:
  - Step A: add/adjust Kotlin tests that encode expected JS parity behavior (initially failing/red).
  - Step B: implement minimal Kotlin changes to make those tests pass (green), then checkpoint.

Test Style Standard (for all remaining slices)
- New Kotlin tests in this migration must follow project conventions:
  - use TestMints flow (`setup { } exercise { } verify { }`) for behavioral tests.
  - use `com.zegreatrob.minassert` assertions (for example `assertIsEqualTo`) instead of raw `kotlin.test` assertions where practical.
  - keep `kotlin.test.Test` annotations for test discovery.
- If a case cannot be expressed cleanly with TestMints, document the reason inline and keep the exception minimal.

Red-Phase Quality Gate (required per slice)
- Before implementing behavior, run the newly added/changed tests and confirm they fail for the intended reason.
- Capture the expected failure signal in slice notes (`tests:` line), including:
  - failing test name(s)
  - key assertion or error proving the missing behavior
- If a test fails for an unexpected reason (wiring, fixture, syntax, unrelated dependency), fix the test setup first and rerun red-phase before feature code changes.
- Only proceed to implementation once red-phase intent is confirmed.
- After implementation, rerun the same scope and record green-phase command/result.

15-Minute Commit Log Plan (resume-friendly)
- [x] Slice 1 (~15m) - Bootstrap library + CLI modules
  - Add reusable analysis library module (`libraries:test-log-analysis`) with API skeleton.
  - Add CLI module (`cli:test-log-tools`) with `main()` + arg parser that calls library stubs.
  - Add commands `validate` and `analyze` as no-op stubs returning success.
  - Commit: `test-log-tools: scaffold kotlin library and cli modules`
  - Resume marker: `NEXT=SLICE_2_PORT_VALIDATE_IO`

- [x] Slice 2 (~15m) - Port validator core parsing + counters
  - Test-first: add validator parity tests for line parsing, non-JSON detection, required-core checks, and failing-violation math.
  - Implement line reader, JSON parse, core counters, offender capture.
  - Support flags: `--strict`, `--fail-on-non-json`, `--fail-on-missing-core`, `--max-offenders`.
  - Implement in library; keep CLI as transport-only wrapper.
  - Match JSON report shape from current JS validator.
  - Pass step: implement until new validator parity tests are green.
  - Commit: `test-log-tools: port validate-test-jsonl core to library`
  - Resume marker: `NEXT=SLICE_3_VALIDATE_PARITY`

- [ ] Slice 3 (~15m) - Validator parity guardrail (dual-run)
  - Test-first: add tests for parity task diff behavior (pass when key metrics match, fail when mismatched).
  - Add Gradle helper task to run both validators and diff key metrics for same input.
  - Keep root `validateTestJsonl` on JS for now; add `validateTestJsonlKotlin` side-by-side.
  - Pass step: implement parity task and make parity-task tests green.
  - Commit: `test-log-tools: add validator parity task`
  - Resume marker: `NEXT=SLICE_4_WIRE_VALIDATE_DEFAULT`

- [ ] Slice 4 (~15m) - Switch Gradle validate task to Kotlin
  - Test-first: add Gradle/task wiring tests or scripted checks proving flags + exit codes match current JS task contract.
  - Change root `validateTestJsonl` from `Exec(node ...)` to `JavaExec` Kotlin command.
  - Keep temporary fallback task `validateTestJsonlJs`.
  - Pass step: switch wiring and make new contract checks green.
  - Commit: `build: route validateTestJsonl through kotlin tool`
  - Resume marker: `NEXT=SLICE_5_PORT_ANALYZE_CORE`

- [ ] Slice 5 (~15m) - Port analyzer core
  - Test-first: add analyzer parity tests for test lifecycle accounting, TestMints detection, phase completeness, and strict/report fail behavior.
  - Implement test lifecycle aggregation and TestMints phase analysis.
  - Preserve strict/report mode and output JSON field names.
  - Implement in library; keep CLI as transport-only wrapper.
  - Pass step: implement until new analyzer parity tests are green.
  - Commit: `test-log-tools: port analyze-test-jsonl core to library`
  - Resume marker: `NEXT=SLICE_6_ANALYZE_PARITY`

- [ ] Slice 6 (~15m) - Analyzer parity + source scan parity
  - Test-first: add source-scan parity tests for `*Test.kt` discovery and skipped directory behavior.
  - Port `collectTestmintsSuiteNames` filesystem walk behavior and skip dirs logic.
  - Add side-by-side parity task (`analyzeTestJsonlParity`) against JS output.
  - Pass step: implement parity task + source scan and make tests green.
  - Commit: `test-log-tools: add analyzer parity checks`
  - Resume marker: `NEXT=SLICE_7_WIRE_ANALYZE_DEFAULT`

- [ ] Slice 7 (~15m) - Switch analyze task to Kotlin
  - Test-first: add Gradle/task wiring tests or scripted checks proving analyze output and strict exit semantics match JS contract.
  - Change root `analyzeTestJsonl` to Kotlin `JavaExec`.
  - Keep temporary fallback `analyzeTestJsonlJs`.
  - Pass step: switch wiring and make new contract checks green.
  - Commit: `build: route analyzeTestJsonl through kotlin tool`
  - Resume marker: `NEXT=SLICE_8_REMOVE_JS`

- [ ] Slice 8 (~15m) - Remove JS scripts + finalize docs
  - Test-first: add final cutover checks ensuring no JS analyzer entrypoints remain in Gradle wiring.
  - Delete `scripts/validate-test-jsonl.mjs` and `scripts/analyze-test-jsonl.mjs` after parity pass.
  - Update README/task log command examples to Kotlin-backed Gradle tasks.
  - Pass step: remove JS scripts and make cutover checks green.
  - Commit: `cleanup: remove node test log analysis scripts`
  - Resume marker: `NEXT=DONE_KOTLIN_CUTOVER`

Continuation Protocol (how to restart from last point)
- At end of each slice:
  - mark completed checkbox in this file,
  - append a one-line status block:
    - `checkpoint: <git-sha>`
    - `next: <NEXT marker>`
    - `verify: <exact command(s) run>`
    - `tests: <red test command + expected failure reason> -> <green test command>`
- Restart command recipe:
  - `git log --oneline -n 20`
  - open this file and continue from latest `next:` marker.

Immediate next slice to execute
- `SLICE_3_VALIDATE_PARITY`

Slice status
- checkpoint: pending-commit
- next: `NEXT=SLICE_3_VALIDATE_PARITY`
- verify: `./gradlew :libraries:test-log-analysis:jvmTest` ; `./gradlew :cli:test-log-tools:compileKotlinJvm`
- tests: `./gradlew :libraries:test-log-analysis:jvmTest` (new parity tests introduced; red during implementation) -> `./gradlew :libraries:test-log-analysis:jvmTest` (green)
