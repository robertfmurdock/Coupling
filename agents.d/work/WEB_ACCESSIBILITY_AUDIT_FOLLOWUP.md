# Extended Web Accessibility Audit

## Goal and Constraints

Audit Coupling beyond the bounded shared-shell and interaction fixes in
[web remediation](WEB_BEST_PRACTICES_REVIEW_REMEDIATION.md). Produce an explicit
route/control inventory, calibrated keyboard and assistive-technology evidence,
and prioritized, independently releasable fixes.

This is a lightweight draft. Define the route set, browser/screen-reader matrix,
severity criteria, and stopping point before playing. Existing automated checks
and simulated keyboard tests are useful evidence, not a complete accessibility
certification. Do not duplicate completed remediation or move regressions introduced
by that card here merely to close it.

## Proposed Safe Slices

- [ ] Inventory route headings, titles, form labels/instructions/errors, image
  alternatives, status announcements, and keyboard interactions. Review existing
  tests first; identify which contracts need component, router, real keyboard,
  or screen-reader evidence. Capture reproducible findings before proposing fixes.
- [ ] Exercise representative authenticated and unauthenticated flows with the
  selected browser/screen-reader combinations. Include focus after navigation,
  modals and contributor popups, zoom/reflow, contrast, and forced-colors behavior.
  Treat these as audit targets, not already-confirmed defects.
- [ ] Prioritize findings and define bounded implementation slices. Reproduce
  each defect at the narrowest interface that observes it, implement the smallest
  correction, and preserve the relevant manual evidence where automation cannot
  prove the contract. Create separate cards for larger independent redesigns.
- [ ] Recheck fixed flows, record residual limitations, and close against the
  agreed inventory rather than expanding into an indefinite polish backlog.

## Flesh Out Before Playing

- Select supported browser/assistive-technology combinations and who can perform
  calibrated screen-reader validation.
- Identify representative party/player/pin configuration, contribution, history,
  statistics, authentication, and integration routes; bound the actual inventory.
- Carry forward concrete residual findings from the original card's closeout,
  with reproduction steps and existing evidence. None are asserted by this draft.

## Validation and Current State

Draft created 2026-11-17; no additional audit or implementation performed here.
Follow Testmints for behavioral tests. Prefer component/router tests for local
navigation; use E2E when a contract depends on assembled routes/auth/services.
Run focused checks and full `fish -lc './gradlew check --no-daemon'` outside the
sandbox for implementation slices. Review tests and production design separately,
commit passing slices together, and record validation gaps without claiming
unperformed manual checks. No push/release authorization is implied.
