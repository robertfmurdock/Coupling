# CPL-62 — Authenticated performance baseline and calibration

## Goal and Constraints

Complete the signed-in and measurement-calibration work transferred from
[CPL-54](../work_completed/CPL-54-WEB_PERFORMANCE_MEASUREMENT_FOLLOWUP.md).
Measure Party List and saved current pairs with known data and repeated cold-cache/
warm runs. Preserve app behavior, visuals and ordinary E2E coverage. No timing
budgets until variability is understood; no production deployment implied.

## Evidence and Starting Points

- `e2e/performance/` owns the opt-in Welcome runner, numeric metric aggregation and
  WebDriver BiDi network totals. Instructions and caveats are in its README.
- `e2e/src/jsE2eTest/.../TestLogin.kt` owns the current Auth0 test cache-seeding
  scheme. Reuse/extract that contract rather than invent a divergent login path.
- Ordinary `E2eSetup.kt` deletes the configured test account's parties. Do not use
  that bootstrap for measurement. Own a named disposable fixture and touch only
  that fixture; report party/player/pair counts needed to assess comparability.
- Existing Gradle E2E execution retrieves its test password from configured AWS
  SSM. Preserve profiles and credential ownership; keep tokens/cache payloads in
  memory and out of logs, reports, source and CLI arguments.
- Welcome cold/warm timing varies; some warm observations have null LCP despite
  support, and some earlier runs recorded module ERR_ABORTED events. Those runs
  are retained, not silently discarded. Final CPL-54 baseline had no failures.

## Safe Slice Sequence

- [ ] Isolate reusable, non-resetting auth/fixture setup. Test its contract at the
  narrowest helper boundary; preserve normal E2E login. Seed fresh browser profiles
  without warming application assets during login, or explicitly label that cost.
  Confirm how the same test identity can coexist with ordinary E2E runs.
- [ ] Measure `/parties/` and `/:partyId/pairAssignments/current/` with a small saved
  fixture (four players, two pairs). Readiness requires fixture data and the usable
  destination action, not merely a header/loading shell. Record fixture counts,
  auth/cache/animation state, actual viewport, browser/host and three repeated
  cold/warm pairs. Use Gradle-owned opt-in automation, not normal timing CI gates.
- [ ] Calibrate observer delivery on a controlled browser fixture, including warm
  refresh, and investigate/classify aborts before excluding any samples. Unsupported
  or missing metrics remain explicit. Preserve original failures/anomalies.
- [ ] Retain full repeated evidence; choose any controlled CPU/network conditions
  explicitly. Add one representative interaction trace if reliable tooling is
  available; label lab interaction observations separately from field INP.

## Completion and Validation

Done when both authenticated routes have reproducible readiness/data contracts,
owned setup/cleanup, retained repeated measurements and documented observer/network
limits. If interaction/field telemetry requires a separate project, scope that
explicitly before playing rather than calling an absent measurement complete.
Use Testmints for helper tests, real browsers for observer/route/auth integration,
then full `fish -lc './gradlew check --no-daemon'` outside the sandbox per slice.
No implementation or measured authenticated baseline exists yet. Use the existing
configured test identity; do not create accounts or perform global fixture resets.
No push/release.
