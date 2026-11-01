# E2E Browser Failure Diagnostics

## Goal and Constraints

Make browser startup and test failures diagnosable from the existing CI artifacts,
including failures before login completes. Preserve the original failure and the
existing test semantics. Do not substantially slow successful E2E runs.

- Keep the successful path lightweight: no extra navigation, sleeps, polling,
  retries, screenshots, DOM dumps, or synchronous file writes per browser event.
- Prefer existing runner hooks, structured logging, and artifact collection.
  Subscribe once per session before navigation; dispose listeners at session end.
- Bound event buffers by entry count and bytes, with truncation/drop counts.
  Capture network failure/error metadata rather than every successful request.
- Take expensive snapshots only on failure, with a bounded total diagnostic
  timeout. Do not repeatedly dump the same shared bootstrap failure for every test.
- Do not capture authentication headers, cookies, tokens, storage contents, or
  request/response bodies. Sanitize URLs before recording query strings/fragments.
- Preserve reusable DynamoDB tables and fresh-ID scenario isolation. This work
  must not introduce table recreation or data scrubbing.

## Evidence and Open Questions

- PR #3443's Build run 41130944248 failed all 56 E2E tests after shared bootstrap
  could not find `Party List`; the DOM contained only an empty `view-container`.
  The underlying cause is unknown; a network/CDN problem is a hypothesis.
- Inspection of failed-run artifacts from September 1 through November 1, 2026
  found one confirmed matching incident. Missing artifacts/reports limit coverage.
- `E2eSetup.kt` collects bootstrap browser logs only after `TestLogin.login()`
  succeeds. Its per-test `finally` can throw a log-check error over a primary error.
- `TestLogin.kt` drains logs on successful login. Review each drain's ownership
  so collection does not silently discard evidence or consume another checker’s data.
- `CheckLogs.kt` recognizes only `ERROR`/`WARN`; raw forwarding does not preserve
  all original metadata. Verify actual classic/BiDi entries and normalize severity,
  timestamps, source, and stack information without weakening existing assertions.
- Generated Chrome config requests browser logs at `ALL`. The wrapper calls
  `browser.getLogs("browser")`; verify this against the actual negotiated protocol
  and installed WebdriverIO version before choosing classic or BiDi collection.
- CI already collects results and uploads `build/test-output/` with `always()`.
  Verify new evidence reaches that artifact, rather than adding another pipeline.
- The plugin's generated screenshot hook does not await `saveScreenshot` and uses
  only milliseconds within a second for names. Check artifact completeness and
  collisions; avoid competing screenshot hooks or unnecessary plugin replacement.

## Safe Slice Sequence

- [x] **Preserve evidence and the primary failure at bootstrap/test boundaries.**
  Tests: a bootstrap exception still flushes browser logs; a test exception survives
  diagnostic failure; successful tests retain current log checks; one shared
  bootstrap failure produces one diagnostic bundle. Expected initial failures:
  startup logs are absent and diagnostic exceptions can replace the primary error.
  Design: E2E harness owns failure handling and invokes best-effort diagnostics
  before losing the test identity/session. Retain secondary diagnostic errors as
  supplemental evidence. Independently useful without network instrumentation.

- [x] **Verify browser capture and add bounded startup/network evidence.**
  Tests: a controlled page emits a console error, uncaught exception, failed resource
  request, and HTTP error before the first application navigation finishes; each
  reaches the artifact with normalized metadata. Cover buffer bounds, cleanup,
  URL sanitization, and duplicate suppression. Expected initial failure: current
  collection lacks reliable pre-navigation/network evidence. Design: one session
  collector using the supported protocol, retaining failure metadata and writing
  through the existing report/log path. Include a small failure-only snapshot of
  sanitized URL, document readiness, and screenshot if the existing hook is reliable.
  Confirm the real browser transport works; fake-event unit tests alone are insufficient.
  Independently useful for failures beyond bootstrap.

- [ ] **Prove artifact delivery and acceptable performance.**
  Validate the complete slice with a deliberately failing startup scenario and
  an ordinary passing suite. Verify diagnostics survive reporting/collection errors
  and appear in the downloaded CI-style artifact. Keep fault injection in a
  focused harness fixture, not a permanently failing application test. If screenshot
  delivery is broken, fix its ownership/awaiting in this slice with focused coverage.
  Record timings, artifact sizes, buffer limits, and remaining gaps; document the
  resulting diagnostic contract and locations. Each earlier slice must already
  include its own relevant verification and performance comparison.

## Recorded Performance Baseline

Three passing pre-collector runs, same application/dependencies/browser:

| Run | Summed testcase seconds | Notes |
| --- | ---: | --- |
| Original harness | 327.299 | 56 passed |
| Slice-one full check | 322.982 | 56 passed; exception boundary only |
| Pre-collector repeat | 328.003 | 56 passed |

Median: 327.299 seconds. Build/container startup excluded. Final comparison still
requires three passing runs with the completed collector and strict policy. Record
new test coverage separately as well as total suite time; do not hide added cost.

## Performance Acceptance

- Before implementation, record at least three comparable passing E2E executions
  with the same browser, dependencies, machine class, and cache state. Measure
  actual test execution separately from compilation, downloads, and Docker startup.
- Compare paired baseline/changed runs. Initial budget: no more than 5% increase
  in median E2E execution time, and no consistent per-test slowdown beyond baseline
  variability. This is a proposed acceptance budget, not a measured result.
- Record individual timings and variation. If noise prevents a conclusion, report
  uncertainty and collect more comparable measurements; do not claim negligible cost.
- Explicitly verify successful tests do not trigger failure snapshots or extra
  browser round trips per event. Avoid high-volume protocol logging or full HAR
  capture by default. Set and test memory/output limits before enabling capture.
- A performance regression requires reducing the cost or revisiting scope with
  the user; do not compensate by weakening tests or increasing retries/timeouts.

## Relevant Paths

- `e2e/src/jsE2eTest/kotlin/com/zegreatrob/coupling/e2e/test/E2eSetup.kt`
- `e2e/src/jsE2eTest/kotlin/com/zegreatrob/coupling/e2e/test/TestLogin.kt`
- `e2e/src/jsE2eTest/kotlin/com/zegreatrob/coupling/e2e/test/CheckLogs.kt`
- `e2e/src/jsE2eTest/kotlin/com/zegreatrob/coupling/e2e/test/E2eJsonlTestMintsReporter.kt`
- `e2e/build.gradle.kts`, `libraries/test-logging/`
- `coupling-plugins/src/main/kotlin/com/zegreatrob/coupling/plugins/reports.gradle.kts`
- `.github/workflows/main.yml`
- Generated `wdio.conf.mjs` / `wdio.conf.d/` and jsmints wrapper/plugin sources
  in the dependency cache are investigation references, not files to edit.
- Related performance work: `TEST_SUITE_PERFORMANCE_IMPROVEMENT_TASK.md`.

## Accepted Implementation Decisions

- User approved strict warning/error failures with no exemptions. Fix warning
  sources, including third-party warnings, rather than allowlisting them.
- Use fish for validation: `/opt/homebrew/bin/fish -lc './gradlew ...'`.
  The SDK password variables exist there; zsh does not load them. Do not ask for
  password values or change credential profiles.
- Collector bounds: 1,000 entries / 1 MiB, 8 KiB per entry, and separate severity
  counters so eviction cannot hide failures. Failure diagnostics have a five-second
  total deadline. No extra work on the normal path beyond bounded event collection.

## Current State

- November 1, 2026; planning baseline commit `b198b1296`.
- Slice one complete: common diagnostic boundary tested on JVM/Node and wired into
  bootstrap/per-test handling. Removed redundant login drain. Independent read-only
  review found no blockers. Commit contains this card, tests, and implementation.
- Slice one commit: `6e705f0ad` (full check passed).
- Slice two complete: bounded session collector, runner failure snapshots,
  classic/BiDi adapters, internal Kotlin bridge, Gradle-driven Node/browser fixtures.
  Full check passed after explicit lockfile-producer wiring and disconnect coverage.
- Next: complete slice-two full check/review and commit before warning-source fixes.
- AWS identity works, and SDK passwords are present under fish (verified without values).

## Validation

- Pre-collector baseline two: 56 passed; 328.003 seconds summed testcase execution.
- Slice-two Node unit tests: 12 passed, including escaped JSON credentials, stalled
  storage/final writes, late screenshots, and runner lifecycle/deduplication.
- Real Chrome/BiDi fixture: passed; verified early console/uncaught/network failures,
  HTTP 503, screenshot, bootstrap deduplication, and sanitized artifacts. Fixture
  teardown was corrected to unsubscribe before deleting the browser session.
- `:e2e:browserDiagnosticsBrowserTest :e2e:collectResults --no-daemon`: passed;
  JSON/PNG artifacts verified under `build/test-output/e2e/results/`.
- First slice-two full check failed after the browser/driver BiDi connection closed
  (1006) during test 48; remaining tests saw invalid session IDs. The collector
  retained nine diagnostic bundles. No cause has been established; this run is not
  performance evidence. Original bundles preserved at
  `/tmp/coupling-disconnected-session-evidence` during investigation.
- Added a red/green disconnect test: protocol selection is fixed at session start;
  lost BiDi connections do not trigger classic polling or dead-session unsubscribe.
  Node unit tests: 13 passed. Follow-up full check passed in 5m19s (56 E2E passed,
  296.591 testcase seconds). Final state full check passed in 5m22s (56 E2E passed,
  290.971 testcase seconds). Resource copy uses the existing process-resources task;
  test inputs declare Node executable/lockfile plus explicit lockfile producer.
- Strict assertion normalization remains deferred until warning sources are fixed;
  slice two preserves the prior assertion policy.

- `:libraries:test-logging:jvmTest`: red, one of four tests failed because diagnostic
  failure replaced the original error; green after preserving/suppressing errors.
- `:libraries:test-logging:jvmTest :libraries:test-logging:jsNodeTest`: passed.
- Unchanged E2E baseline one passed: 56 tests, 327.299 seconds summed testcase
  execution (`/tmp/coupling-e2e-baseline-1.xml`). Build/setup is excluded.
- Slice-one full `check --no-daemon` under fish: passed in 7m28s, including SDK,
  browser, E2E and structured-log validation. E2E: 56 passed; 322.982 seconds
  summed testcase execution. A single comparison does not establish overhead.
- Slice-one code/test review passed; `git diff --check` passed.

- Planning only: inspect card content and run `git diff --check`. No runtime tests
  are required for this documentation-only change.
- Implementation: focused Gradle checks during red–green–refactor, then
  `./gradlew check --no-daemon` before each test-plus-implementation commit.
- Browser-bearing commands, including focused E2E runs and full checks, must run
  outside the sandbox through the approved mechanism with a fresh daemon.
- Express any new fixture/validation automation as Gradle tasks. Record commands,
  results, performance evidence, and validation gaps per slice. Do not call a slice
  release-safe or commit it while the required full check is failing.
- No push or release is authorized by this work card.
