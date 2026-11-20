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

## Status

Planned handoff; no implementation or validation yet. No visual-evidence issue is
needed unless the work changes product visuals. Follow [Testing](../context/TESTING.md)
and the [Gradle playbook](../context/GRADLE_PLAYBOOK.md).
