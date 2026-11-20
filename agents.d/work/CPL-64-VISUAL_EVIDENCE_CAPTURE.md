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
Small local bindings cover missing jsmints viewport/emulation/screenshot methods;
the only JS policy-free glue connects launcher hooks to exported Kotlin methods.

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

Implementation and documentation are validated; implementation commit and card
closeout are the remaining bookkeeping steps.

No product visuals changed, so no visual-evidence issue/upload is required.
See [usage and limitations](../../e2e/visual-evidence/README.md).
