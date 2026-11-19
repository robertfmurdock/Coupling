# CPL-54 — Web Performance Measurement and Optimization

## Goal and Constraints

Establish comparable browser performance evidence for Coupling's main entry
flows, then implement only optimizations supported by that evidence. Transferred
from [web remediation](../work_completed/CPL-51-WEB_BEST_PRACTICES_REVIEW_REMEDIATION.md); that card has not
established a performance baseline or claimed measured improvements.

Lightweight draft: refine route selection, measurement conditions, and acceptance
criteria before playing. Keep Auth0, analytics, GraphiQL, integrations, GraphQL,
and WebSocket behavior functional. Coordinate resource changes with the separate
[CSP follow-up](CPL-52-WEB_CSP_HARDENING_FOLLOWUP.md) and its CDN-loading constraints.

## Proposed Safe Slices

- [ ] Establish a reproducible baseline for Welcome, authenticated party list,
  and pair assignments, confirming exact URLs and fixture data first. Record
  viewport, browser, auth state, network/CPU conditions, cold/warm cache, repeated
  runs, and variation. Capture LCP, CLS, transfer size, long tasks, and interaction
  traces; distinguish lab interaction evidence from field INP.
- [ ] Select the highest-value demonstrated bottleneck. Investigate CSS/fonts,
  GraphiQL, popup/drag-drop code, analytics, image loading, and generated CDN
  imports as candidates, without assuming any needs deferral or self-hosting.
  Define one user-visible readiness contract and expected regression before
  implementing one independently releasable optimization.
- [ ] Re-measure under the same conditions, retain before/after artifacts, and
  validate affected routes. Repeat only for other justified bottlenecks. Set
  durable budgets only after understanding measurement variability.

## Flesh Out Before Playing

- Choose stable test data, authenticated setup, artifact location, and measurement
  tooling; express recurring repository automation as Gradle tasks.
- Agree which metrics/readiness outcomes justify a change and when this card ends.
- Inspect `client/vite.config.mjs`, `cdn.json`, the HTML template, image registry,
  production bundle reports, and GraphiQL's embedded document.
- Inspect which obsolete popup resources remain after the native notification
  dialog migration; confirm usage before removing dependencies.

## Validation and Current State

Draft created 2026-11-17. Obsolete popup resource cleanup is complete below;
comparable browser performance baselines and measured optimization remain open.
Use the narrowest meaningful component/router tests for behavior, adding assembled
browser coverage when necessary. Do not substitute source assertions or Lighthouse
scores for user-visible readiness. Run focused checks and full
`fish -lc './gradlew check --no-daemon'` outside the sandbox for implementation
slices; commit tests and implementation together. Record evidence, risks, and
commit IDs. No push/release authorization is implied.


## Active Resource Cleanup Slice

Continued after `84fc951d3`. CPL-56 remains open for its uncompleted data,
reflow and query-state findings. Before the larger measurement effort, address a
source-proven obsolete resource (explicit candidate under this card), without
claiming a measured loading or interaction gain.

- [x] Remove unused reactjs-popup bindings, package/version constraint, generated
  CDN map entry and startup CSS import; regenerate dependency state using Gradle.
  Existing contributor Popper and native notification-dialog tests preserve the
  active behavior. No failing test invented for dead-code/dependency removal.
- [x] Inspect rebuilt production import map/bridge/assets and run focused/full
  checks before committing. Keep this card open for comparable browser baselines.

Before evidence: production import map has 28 entries, including
`https://esm.sh/reactjs-popup@2.0.6` (with shared dependency/external parameters).
Main imports `reactjs-popup/dist/index.css` unconditionally. Installed stylesheet
source is 517 bytes, scoped to popup-content/overlay/arrow classes. No runtime
consumer of those classes or the legacy bindings remains in repository source.
Active contributor popup uses locally styled CouplingPopUp/react-popper; native
notification dialog does not use this package. This is static artifact evidence,
not network transfer bytes or a native trace. Preserve all active popup styles and
placement. Modern Web Guidance performance: “DON'T load invisible or unreachable
CSS/JS.” Validation pending; no push/release authorization.


Cleanup validation: the first focused run stopped at the expected changed-lockfile
gate. `kotlinUpgradeYarnLock` succeeded; its only lock change removes the five-line
reactjs-popup entry, with no unrelated package upgrades. Production CDN generation,
production Vite build, components/jsdom (137 tests), and application Chrome tests
(52 tests) pass. Rebuilt production import map has 27 entries (previously 28),
without reactjs-popup. Inspection of all emitted HTML/JS/MJS/CSS found zero legacy
package/global or popup-content/overlay/arrow class references. Active contributor
popup and native notification implementations were untouched.

Full `fish -lc './gradlew check --no-daemon'` passes (690 tasks), log
`/tmp/coupling-popup-resource-check.log`. Dependency/reference and production-design
reviews confirm this is removal of obsolete loading and bindings, not a component
replacement. Existing interaction, focus, layout, native-dialog and assembled
application tests remain intact; no artificial source-string test added. Diff
checked. No native network trace, LCP/CLS/INP improvement, or transferred-byte
reduction is claimed from the source stylesheet size/import count. No push/release
performed. Next slice: choose repeatable baseline routes/conditions and measurement
harness before selecting any remaining active-resource optimization.
