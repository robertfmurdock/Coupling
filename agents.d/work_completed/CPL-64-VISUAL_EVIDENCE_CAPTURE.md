# CPL-64 — Capture repeatable visual evidence through Gradle

**Work card protocol: 2**

## Goal and scope

Provide an opt-in Gradle task for labeled screenshots of named local fixtures,
without editing product tests or adding capture side effects to `check`.
CPL-23 required temporary E2E bindings and several rebuilds to collect evidence;
requested mobile window sizes also differed from actual capture dimensions.

Start with the contribution overview fixture and desktop/narrow captures. Reuse
the local stack, authentication, browser lifecycle, and diagnostics in `e2e/`.
No product UI changes, pixel-diff service, automatic issue posting, or new shell
automation.

## Acceptance evidence

- One documented `./gradlew :e2e:<capture-task>` invocation selects a fixture,
  capture label and viewport and writes PNGs plus a manifest under
  `e2e/build/reports/visual-evidence/`.
- Manifest records source SHA/dirty state, fixture identity/seed, label, theme,
  browser/version, device scale, requested and actual viewport, image dimensions,
  paths, and completion/failure.
  Exclude credentials, cookies, tokens, and authentication payloads.
- Stable fixture data and an observable readiness condition make Before/After
  captures comparable. A narrow capture verifies actual viewport dimensions;
  report unsupported sizes rather than silently accepting a larger window.
- Failed/interrupted runs cannot masquerade as complete or leave browsers running.
  Repeated labels have a documented overwrite policy. Normal test behavior stays
  unchanged.
- Fresh-checkout instructions cover capture, output inspection, and
  `gh issue comment --attach`; upload remains an explicit operator action.

## Safe implementation slice

Owner: `e2e/` task registration and capture harness. Inspect `e2e/build.gradle.kts`,
`e2e/performance/`, browser diagnostics, and contribution overview fixtures first.
Choose the task API and fixture seam from those patterns; avoid duplicating auth.

Call the behavior/design shot before tests. Keep manifest, validation, and failure
contracts in focused Node tests: unknown fixture, mismatched viewport, and partial
run must not report successful evidence. Confirm missing behavior fails meaningfully.
Use a real-browser integration check for fixture readiness, measured viewport,
image dimensions, and cleanup, which Node doubles cannot establish. Implement the
task, one named fixture, harness, tests, and usage docs together so the slice is
useful on its own. Run focused checks, configuration-cache verification, and the
full `./gradlew check` gate before committing.

The capture task is opt-in and must execute when requested; do not reuse stale
screenshots from the build cache. Declare configuration inputs and output ownership
using existing Gradle conventions. Run browser tasks outside the sandbox with a
fresh daemon. Link [work-card evidence guidance](../context/WORK_CHECKLIST.md#record-visual-evidence)
and update its pending-task note when the task ships.

## Design and ecosystem decisions

One slice owned by `e2e/`: opt-in `captureVisualEvidence`, Kotlin manifest/capture
policy in `jsMain`, Testmints Node tests, and an isolated fixture compilation
sharing existing authentication/page sources. The public WDIO task and config
fragment seams support capture without a second browser runner or new libraries.
Small local bindings cover missing jsmints viewport/emulation/screenshot methods.
Native WDIO capture config loads the generated base config and invokes the Kotlin
hook composer; lifecycle sequencing and failure handling live in Kotlin.

User direction: prefer Kotlin for consistency and reuse; treat interop friction
as an ecosystem improvement opportunity. Keep this as a candidate for a future
top-level repository objective. Do not extract speculative shared libraries.

Unique run directories preserve repeated labels. Lifecycle is
`partial -> captured -> complete`, with completion after WDIO cleanup; failure or
interruption never promotes incomplete evidence. Fixture-only Save Timestamp
display normalization is disclosed because the service owns creation times.

## Validation and review

- Initial Node contract red: placeholder left runs partial and accepted unknown
  fixtures/mismatched viewports. Contracts then moved to Kotlin per user direction.
- Kotlin contract sensitivity: removing viewport mismatch rejection failed
  `unsupportedViewportRecordsActualSizeWithoutPhotographing`; restored guard.
- Eight Kotlin contracts passed; real Chrome verified 390x844 viewport/PNG at
  scale 1. Assembled authenticated narrow and desktop captures succeeded.
- First Compose attempt hit transient Serverless DNS resolution; retry succeeded.
- Combined focused run exposed dashboard collector overlap with capture JUnit
  output. Capture runner output moved outside normal test-results ownership.
- Focused Kotlin contracts, real-Chrome diagnostics/browser test and authenticated
  desktop capture passed together after fixing output ownership. Repeating the
  identical invocation reused configuration cache, executed capture again, and
  preserved both runs in distinct directories. Desktop PNG SHA-256 was identical
  across both runs: `ba13777208d4d0ecbafe3bfc8ff69728b6ee709d8d52c7f7aada45448530b3f7`.
- Chrome 158.0.8127.36 captured actual/PNG 1280x900 and 390x844 at scale 1.
  Inspected both images; overflow at narrow width is preserved, not repaired.
- Unknown-fixture integration exited nonzero with `failed`/`validation` manifest.
  Ctrl-C during worker startup exited 130 and retained `partial` evidence. No
  additional Chrome/driver processes remained compared with the pre-run baseline.
  SIGKILL/host crashes cannot guarantee process cleanup; documented explicitly.
- First full gate ran from zsh without SDK test passwords and failed SDK auth.
  Presence-only diagnosis confirmed this host's documented fish credential loader.
  `fish -lc './gradlew check --no-daemon'` then passed: 694 tasks, including SDK
  Node/JVM, browser, normal E2E, lint, and test-log validation. No auth code changed.
- Test review: contract scenarios observe manifests and lifecycle outcomes; the
  viewport fault check proved sensitivity. Real Chrome owns geometry/PNG evidence;
  authenticated task runs establish the named fixture's assembled readiness.
- Production review: Kotlin owns policy, manifests and fixture flow; 28 lines of
  JavaScript adapt launcher hooks. Existing WDIO owns sessions/drivers, SDK helpers
  own auth, and no new npm dependency or product source/test edits were needed.

Completed in implementation commit `963e8d188abc1ff3812ebb41505c5b8593850a1e`.
The subsequent card-closeout change is documentation only; the passing full gate
above covers the unchanged implementation. Nothing was pushed or released.

No product visuals changed, so no visual-evidence issue/upload is required.
See [usage and limitations](../../e2e/visual-evidence/README.md).

## Testmints refactoring follow-up — 2026-11-20

Review found that the partial-state assertion could be swallowed by the expected
failure capture, runner completion was hidden in verification, and setup could
leak its directory if manifest acquisition failed.

- Capture and successful runner completion now have separate Node scenarios.
  Verification only observes outcomes, including the persisted manifest.
- The preparation callback records its intermediate status and throws a known
  exception. Verification outside `runCatching` checks both the status and the
  original exception, preventing assertion/setup failures from posing as success.
- Node fixture acquisition unwinds resources on setup failure and preserves the
  primary error. Browser readiness/manifest arrangement moved into setup, with
  cleanup if that setup fails; exercise now contains only capture.
- Sensitivity check: temporarily marking evidence `captured` before preparation
  made the strengthened partial-state assertion fail; the production fault was
  restored. A separate deliberate failure after resource acquisition failed all
  nine Node capture scenarios with the original error and left no new fixture
  directories. That temporary fault was also restored.
- Formatting, all nine Node capture contracts, and the real-Chrome geometry/PNG
  check pass. Full `fish -lc './gradlew check --no-daemon'` passed: 684 tasks,
  configuration cache reused. Existing unchanged checks retained normal cache
  behavior; no cache policy or test filtering changed.
- Final test review confirms lifecycle triggers are in exercise and intermediate
  observations are verified outside failure capture. Production review confirms
  no implementation diff remains after restoring the sensitivity fault.

This follow-up changes test code only; capture implementation, Gradle task wiring,
and product behavior remain unchanged.

## Kotlin launcher hooks follow-up — 2026-11-20

User preference: keep native WDIO configuration JavaScript where needed, but move
the separate `capture.mjs` launcher adapter into Kotlin.

- Removed `e2e/visual-evidence/capture.mjs`. `CaptureHooks.kt` owns hook composition,
  option preservation, manifest publication before startup, and completion/error
  sequencing. Only `CaptureHooks` needs a JavaScript export; lifecycle helpers are
  internal Kotlin code.
- Six Testmints Node scenarios cover preserved Mocha options, hook argument/order
  forwarding, synchronous/asynchronous hooks, native JavaScript error identity,
  absent original hooks, and rejecting incomplete evidence. A temporary fault
  replacing the runner's exit code with zero failed the forwarding scenario;
  restored before validation.
- Real capture exposed a pre-existing plugin seam: config fragments load
  concurrently, and the HTML reporter can overwrite an earlier `onPrepare`.
  The former capture fragment depended on import timing. Seven lines of native
  `wdio.visual-evidence.conf.mjs` now load the complete generated config before
  applying Kotlin hooks. No runner configuration, auth, or browser driver is
  duplicated; no upstream plugin or new dependency is required.
- Gradle owns the new native config as an input, selects it only for opt-in
  capture, and passes the generated base-config location. The obsolete capture
  hook URL and fragment were removed. Normal E2E configuration stays on the
  plugin's generated config.
- Focused Node tests and authenticated 390x844 capture passed, including HTML
  report completion. The resulting PNG is byte-identical to the earlier narrow
  capture: SHA-256 `057efa2eee4a9303e79b032e731ac16c8acd15896ca0de9bf7af234946afc432`.
  Repeating the invocation reused configuration cache and produced fresh evidence.
- Full `fish -lc './gradlew check --no-daemon'` passed: 694 tasks, including normal
  E2E, browser diagnostics, lint and test-log validation. Test review confirms
  explicit hook actions and independently verified outcomes; production review
  confirms the native config only loads/delegates, with all hook behavior in
  Kotlin and no remaining `capture.mjs`/obsolete hook-URL references in `e2e/`.

Browser-executed JavaScript snippets in Kotlin strings are deliberately outside
this follow-up; they are a separate browser callback/compilation interop concern.

## Shared source sets and operator docs — 2026-11-20

Replaced capture's Kotlin filename allowlist and diagnostics' capture-spec
exclusion with source-set dependencies. `browserTestSupport` owns reusable auth
and page helpers for E2E/capture; `browserCaptureSupport` owns the WDIO adapter for
capture/browser diagnostics; `diagnosticsTestSupport` is now a source set shared
by Node/browser diagnostics. Each compilation keeps its own scenarios in its
default directory. Shared dependencies live with support instead of capture
inheriting the E2E test configuration. Eleven moved Kotlin files are byte-identical.

Reduced the capture README from 115 to 58 lines, retaining operator commands,
artifact/status interpretation, comparison conditions, concurrency constraints,
and explicit upload instructions. Removed implementation narrative and validation
commentary; its relative documentation links resolve.

All four compilations and focused checks passed. Real 390x844 authenticated
capture and browser diagnostics passed; a repeated invocation reused configuration
cache and wrote fresh evidence. Source inspection confirms no Kotlin filename
include/exclude rules or cross-compilation source-directory injection remain in
`e2e/build.gradle.kts`, and shared support directories register no tests.
Full `fish -lc './gradlew check --no-daemon'` passed: 697 tasks, including normal
E2E and test-log validation. The capture PNG remains byte-identical to the prior
narrow evidence. Test review confirms unchanged scenario bodies and registration
boundaries; production/build review confirms shared code and dependencies follow
source-set ownership, with no new libraries or copied implementations. Updated
the existing diagnostics card's moved `TestLogin.kt` reference.
