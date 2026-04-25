# Task: Add `testmints` test markers to e2e logs

Goal
- Ensure `:e2e:e2eRun` emits `testmints` lifecycle markers into `build/test-output/test.jsonl`.
- Restore phase-level visibility (`setup`, `exercise`, `verify`) for e2e analysis.
- Keep log output directly queryable by `jq`/Splunk without custom parsing.

Problem statement
- Current e2e entries in `test.jsonl` are `Log`-only (primarily `command`, `browser`, `forwarded-output`).
- No `testmints` markers are present for e2e, so phase timing and per-test attribution are not available from logs.
- This blocks high-confidence e2e bottleneck analysis.

Success criteria
- e2e test execution emits `testmints` markers with canonical phase names:
  - `setup-start` / `setup-finish`
  - `exercise-start` / `exercise-finish`
  - `verify-start` / `verify-finish`
- Markers are emitted with stable test identity fields (`run_id`, `task`, `suite`, `test`; and `test_id` if available).
- `analyzeTestJsonl` includes e2e phase counts and no strict violations.
- Existing sdk/jvm/js logging behavior remains unchanged.

Non-goals
- No server-side changes.
- No e2e test behavior/semantics changes.
- No command-path performance tuning in this task.

Implementation slices

- [ ] Slice 1 - map current e2e logging path
  - Trace e2e test execution/log pipeline:
    - e2e test runtime entry points
    - existing `CheckLogs.kt` ingestion
    - listener/writer path to JSONL
  - Document where `testmints` events should be emitted or forwarded.

- [ ] Slice 2 - e2e testmints emission
  - Add/enable `testmints` markers in e2e test harness at semantic phase boundaries.
  - Use canonical phase names to match analyzer expectations.
  - Ensure emitted records include task/platform/run context.

- [ ] Slice 3 - normalization and persistence
  - Ensure e2e-emitted `testmints` events reach JSONL unchanged (or canonically normalized) via current log listener path.
  - Confirm no marker loss across browser-to-runner forwarding.

- [ ] Slice 4 - strict validation + tests
  - Add/adjust analyzer/validator tests to cover e2e `testmints` presence and phase integrity.
  - Keep strict-mode posture: missing required phases in expected e2e tests should be visible in report/violations.
  - Preserve existing sdk parity tests.

- [ ] Slice 5 - query examples and diagnostics
  - Add `jq` examples for e2e phase timing and counts.
  - Add a quick diagnostic query to detect regressions (e.g., e2e run with zero `testmints` events).

Verification commands
- `./gradlew :e2e:compileE2eTestKotlinJs --no-configuration-cache`
- `./gradlew resetTestJsonl --no-configuration-cache`
- `./gradlew :e2e:e2eRun --rerun-tasks --no-configuration-cache`
- `./gradlew analyzeTestJsonl --no-configuration-cache`
- `./gradlew :libraries:test-log-analysis:jvmTest --tests "*AnalyzeCommandParityTest*" --tests "*ValidateCommandParityTest*" --no-configuration-cache`

Suggested jq acceptance checks
- e2e emits `testmints`:
  - `jq -r 'select(.task==":e2e:e2eRun" and .logger=="testmints") | .message' build/test-output/test.jsonl | wc -l`
  - expected `> 0`
- e2e phase markers present:
  - `jq -r 'select(.task==":e2e:e2eRun" and .logger=="testmints") | .properties.phase' build/test-output/test.jsonl | sort | uniq -c`
  - expected includes all 6 canonical phases
- analyzer reports e2e phase counts:
  - `./gradlew analyzeTestJsonl --no-configuration-cache`
  - expected no strict violations attributable to missing e2e phase markers

Risks / pitfalls
- Browser log forwarding may strip structured fields unless explicitly preserved.
- Phase boundaries may be ambiguous in existing e2e harness; enforce deterministic placement.
- Async test cancellation paths may miss `*-finish` markers; guard with `finally` semantics.
- Additional logging volume should remain bounded and not materially slow e2e runs.

Definition of done
- Fresh e2e run writes canonical `testmints` phase events to `test.jsonl`.
- Analyzer consumes these events and reports meaningful e2e phase counts with strict mode clean.
- Regression query catches future loss of e2e `testmints` emission.

Continuation status
- checkpoint: working tree (uncommitted)
- next: `NEXT=SLICE_1_MAP_E2E_LOGGING_PATH`
