# CPL-62 — Authenticated performance and route-scoped CDN loading

**Work card protocol: 2**

## Goal and Constraints

Complete the signed-in and measurement-calibration work transferred from
[CPL-54](../work_completed/CPL-54-WEB_PERFORMANCE_MEASUREMENT_FOLLOWUP.md).
Measure Party List and saved current pairs with known data and repeated cold-cache/
warm runs. Preserve app behavior, visuals and ordinary E2E coverage. No timing
budgets until variability is understood. The baseline is independently deliverable.
After calibration, use it to assess one route-only CDN dependency group. Preserve
shared React identity, all actions and the established visuals and animations.

## Scope Boundary and Tooling

This performance work is independent of CSP. [CPL-52](CPL-52-WEB_CSP_HARDENING_FOLLOWUP.md)
owns policy, script authorization and security compatibility checks against the
current dependency graph; it does not require route-only loading or performance
calibration first. This card owns performance measurement and optional CDN
loading changes. Existing inventories can inform both without merging delivery.

Follow [tooling purpose and lifetime](../context/GRADLE_PLAYBOOK.md#tooling-purpose-and-lifetime).
Reuse existing measurement capabilities and application tests. Evidence requests
below do not require a new recorder, browser fixture framework, observer subsystem
or persistent experiment script. Document one-off findings and limitations;
justify any retained tool extension by a concrete recurring need in the slice.

## Acceptance Evidence

- Reproduce Party List and saved current-pairs readiness with an owned fixture
  and three cold/warm pairs per route. Keep observer limitations, aborts and
  missing samples visible alongside measurements.
- For one selected dependency group, show absence on startup and correct
  loading on first use in a production build. Retain matched request, byte and
  emitted JavaScript comparisons plus action and recovery evidence. A runtime
  inventory that rules out a safe candidate can close the loading investigation.

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

- [ ] Isolate reusable, non-resetting auth/fixture setup. At the helper boundary, test that setup leaves unrelated parties intact
  and cleanup targets only the named fixture. Preserve normal E2E login. Seed fresh browser profiles
  without warming application assets during login, or explicitly label that cost.
  Confirm how the same test identity can coexist with ordinary E2E runs.
- [ ] Measure `/parties/` and `/:partyId/pairAssignments/current/` with a small saved
  fixture (four players, two pairs). Readiness requires fixture data and the usable
  destination action, not merely a header/loading shell. Record fixture counts,
  auth/cache/animation state, actual viewport, browser/host and three repeated
  cold/warm pairs. Reuse existing opt-in Gradle measurement capabilities; do not
  introduce normal timing CI gates or a new runner just to obtain this evidence.
- [ ] Establish measurement limitations, including warm refresh, using existing
  tooling and bounded experiments. Investigate/classify aborts before excluding
  samples. Unsupported or missing metrics remain explicit. Preserve original
  failures/anomalies; a dedicated observer fixture suite is not a deliverable.
- [ ] Retain full repeated evidence; choose any controlled CPU/network conditions
  explicitly. Add one representative interaction trace if reliable tooling is
  available; label lab interaction observations separately from field INP.

## Route-Scoped CDN Loading

`client/src/jsMain/resources/index.html` eagerly imports every configured CDN
namespace and assigns it to window. Meanwhile, emitted lazy route chunks have
real ESM imports. This suggests possible redundant startup loading of active
chart, clipboard or drag/drop libraries. It does not prove that removing every
namespace assignment is safe: audit globals, side effects, peers, GraphiQL and
first-use behavior first. Preserve inline/server-injected configuration timing.

The retained final Welcome baseline has 323 CDN module responses per load. Removing
unused entries reduced requests without local bundle growth; remaining packages
have active consumers. Do not apply a blanket CDN bundling or self-hosting change.

### Safe Slice Sequence

- [ ] Inventory generated startup/route imports and global/side-effect consumers.
  Choose one active route-only package group. Declare Welcome readiness and the
  affected action/route contract; consult current Modern Web Guidance.
- [ ] Add production-built browser evidence showing that the chosen package is
  absent from startup and loads when actually needed, while the feature works.
  Keep detailed component behavior below E2E. Confirm a meaningful failure before
  changing loading. A source-string test or lower request count alone is insufficient.
- [ ] Defer that group with existing module/route mechanisms. Retain import maps
  and externals where required so code is not simply rebundled locally. Preserve
  singletons and server/dev configuration; obtain approval for any visible changes.
- [ ] Compare all emitted JS including lazy chunks and network bytes/request counts
  under matched conditions. Validate first use, repeat use, and error/loading
  recovery on affected routes; retain before/after evidence and full-suite results.

Reuse the generated import map and runtime request inventory from
[CPL-52](CPL-52-WEB_CSP_HARDENING_FOLLOWUP.md) where conditions match. Coordinate
script and header changes there if this work affects their compatibility. CSP
implementation is not a prerequisite for performance investigation, and CDN
deferral is not a prerequisite for CSP. Avoid blanket bundling or self-hosting changes.

## Resume Note

Start by evaluating whether existing auth/fixture capabilities can support the
bounded measurements without resetting unrelated data. Signed-in measurements,
measurement-limit investigation and loading implementation are pending. Keep
field telemetry as a separate decision if reliable interaction tooling is unavailable.
