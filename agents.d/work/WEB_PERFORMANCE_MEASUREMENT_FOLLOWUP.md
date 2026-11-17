# Web Performance Measurement and Optimization

## Goal and Constraints

Establish comparable browser performance evidence for Coupling's main entry
flows, then implement only optimizations supported by that evidence. Transferred
from [web remediation](WEB_BEST_PRACTICES_REVIEW_REMEDIATION.md); that card has not
established a performance baseline or claimed measured improvements.

Lightweight draft: refine route selection, measurement conditions, and acceptance
criteria before playing. Keep Auth0, analytics, GraphiQL, integrations, GraphQL,
and WebSocket behavior functional. Coordinate resource changes with the separate
[CSP follow-up](WEB_CSP_HARDENING_FOLLOWUP.md) and its CDN-loading constraints.

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

Draft created 2026-11-17; no measurements or implementation performed here.
Use the narrowest meaningful component/router tests for behavior, adding assembled
browser coverage when necessary. Do not substitute source assertions or Lighthouse
scores for user-visible readiness. Run focused checks and full
`fish -lc './gradlew check --no-daemon'` outside the sandbox for implementation
slices; commit tests and implementation together. Record evidence, risks, and
commit IDs. No push/release authorization is implied.
