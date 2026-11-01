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

- [x] **Enforce strict policy, fix warning sources, and prove artifact delivery/performance.**
  No warning exemptions. Fix cancellation severity/lifecycle metadata and replace
  the deprecated drag/drop package with the maintained named polyfill API, CDN
  mapping and default styles. Exercise actual touch player swapping and warning,
  error, informational, and bootstrap-check failure policies.
  Validate the complete slice with a deliberately failing startup scenario and
  an ordinary passing suite. Verify diagnostics survive reporting/collection errors
  and appear in the downloaded CI-style artifact. Keep fault injection in a
  focused harness fixture, not a permanently failing application test. If screenshot
  delivery is broken, fix its ownership/awaiting in this slice with focused coverage.
  Record timings, artifact sizes, buffer limits, and remaining gaps; document the
  resulting diagnostic contract and locations. Each earlier slice must already
  include its own relevant verification and performance comparison.

## Recorded Performance Results

Local Chrome 156.0.8078.80 (mac), WebdriverIO 10.1.0, Gradle-managed Node 24.15.0.
Times sum individual JUnit testcase durations, using **class + method** identities
because a method name is reused across classes. Compilation/container startup is excluded.

| Run | Before collector (56 tests) | Completed strict harness (61 tests) |
| --- | ---: | ---: |
| 1 | 327.299 s | 298.695 s |
| 2 | 322.982 s | 293.767 s |
| 3 | 328.003 s | 287.663 s |
| Median | 327.299 s | 293.767 s |

Observed median change: **-10.245%**, inside the +5% overhead budget, including
five added cases. None of the 56 existing cases had all three final durations
above its observed baseline range. The new real touch case took 8.761 / 8.662 /
8.309 seconds. Passing run logs were 678,244 / 686,900 / 686,517 bytes (30,817 /
30,923 / 30,871 bytes gzipped). Buffers remain bounded; normal passing application
tests take no diagnostic screenshots or page snapshots.

Measurements were collected before/after implementation rather than strictly
alternating. Local run variability and the different test counts limit causal
speedup claims; this establishes no observed local regression, not a CI timing
guarantee. The intermediate disconnected-browser failure was excluded from timing
samples and recorded below. XML evidence was retained during implementation as
`/tmp/coupling-e2e-{baseline-1,slice-one,baseline-2,final-1,final-2,final-3}.xml`.

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

## Completed State

- Completed November 1, 2026, on `codex/e2e-browser-diagnostics`.
- Baseline: `b198b1296`. Slice one: `6e705f0ad`. Slice two: `509355389`.
  The completion commit containing this card delivers slice three and its tests.
- Implemented startup capture, original-error preservation, bounded session-level
  evidence, strict browser severity checks without exemptions, cancellation-aware
  lifecycle/error logging, maintained mobile polyfill/binding/CDN/styles, actual
  touch swap coverage, and the explicit FGREP task/configuration-cache input.
- Independent test/design reviews found and verified fixes for escaped structured
  secrets, total deadlines including storage, late screenshot effects, bootstrap
  log-failure deduplication, and touch cleanup error preservation. No open findings.
- User guidance is recorded in `agents.d/context/TESTING.md`, including fish-based
  local credential loading, diagnostic locations, harness tasks and focused filters.
- No implementation work remains. No push or release performed or authorized.
- CI execution/performance remains unmeasured; the original PR's rare blank-page
  cause is still unknown. This work improves evidence rather than asserting its cause.

## Validation

- Four common diagnostic-boundary scenarios passed on JVM and Node: successful
  action, failed bootstrap with one collection, primary + diagnostic failures, and
  successful action rejected by failed log checks. Primary-replacement regression
  was confirmed red before implementation.
- Thirteen Gradle-driven Node collector tests passed: classic/BiDi adapters,
  metadata/identity, entry/byte bounds and eviction counters, network field exclusion,
  escaped Chrome/BiDi payload redaction, bootstrap deduplication, listener cleanup,
  absolute deadlines including storage/final writes, late screenshots and lost BiDi.
  Timeout tests advance a mock clock after the intended stage, avoiding disk-speed races.
- Real Chrome fault fixture passed and generated JSON + PNG evidence for console
  warnings/errors, uncaught script exception, failed fetch and HTTP 503 before
  bootstrap. `:e2e:collectResults` delivery was verified under the existing artifact root.
- Controlled real touch fixture passed without browser warnings. Closing its server
  connections removed a 60-second keepalive delay; final fixture time was about 2.8 s.
- Real Appender tests confirmed cancellation propagates without ERROR and uses
  `command_error=false` / `command_cancelled=true`; ordinary exceptions still log
  ERROR with their original cause and propagate. Both regressions were red/green.
- Four browser log-policy scenarios passed, including successful startup followed
  by a strict-log failure being marked for a single bundle. The real-app touch test
  checks exact player memberships after a W3C touch gesture; viewport and pointer
  cleanup preserve failures. Movement exceeds Chrome touch slop and waits for dragend.
- FGREP cache bug was reproduced (stale filter selected the prior scenario), then
  fixed with one tracked provider feeding both task input and environment. Changed
  filters invalidated configuration reuse; focused counts and unfiltered 61-test
  reports were checked. Cache reuse remains enabled.
- Slice-one `check --no-daemon` passed (7m28s); slice-two final gate passed (5m22s).
  The first slice-two attempt lost its browser/driver connection (1006) during test
  48; nine bundles retained the evidence. Cause remains unknown. Protocol selection
  now remains fixed after connection loss; subsequent full checks passed.
- Final strict `check --no-daemon` passed (6m28s), including all 61 E2E cases, all
  other suites, and structured-log validation. Two further unchanged 61-test runs
  passed (5m18s / 5m04s task wall time). Final full gate passed again (13s with valid
  unchanged results reused). Commands ran through fish, browser commands outside
  the sandbox with fresh daemons. `git diff --check` passed.
