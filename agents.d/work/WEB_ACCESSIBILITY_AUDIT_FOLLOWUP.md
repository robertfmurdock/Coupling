# Extended Web Accessibility Audit

## Goal and Constraints

Audit Coupling beyond the bounded shared-shell and interaction fixes in
[web remediation](../work_completed/WEB_BEST_PRACTICES_REVIEW_REMEDIATION.md). Produce an explicit
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

## Closeout Review Inputs

- User feedback (2026-11-17): reconsider the shared skip link on sparse Welcome
  screens, where there is no repeated navigation to skip. The user accepted the
  Get started view and explicitly deferred this design question. Review the main
  landmark boundary as well: the current template wraps the whole React mount,
  including route-owned navigation. Keep this as a deliberate design review,
  not a claim that the current link skips every navigation control.
- DemoPageFrame marks the simulated application pointer-events:none while
  advertising that its controls do not work. Audit keyboard/assistive-technology
  treatment of those controls; pointer-event suppression alone is not inertness.
- Shared player portraits still use the generic alt text "player-icon" beside
  visible player names. Evaluate decorative alternatives/contextual names at
  their actual link and card owners.
- Shared data loading collapses failed/null results into the same recovery UI.
  GraphiQL has a distinct authorization loader. Review error distinctions and
  calibrated announcements separately before claiming all errors are covered.
- Original closeout did not manually visit saved-party/player/pin routes: the
  local signed-in account had no parties, and no new data was saved for review.
  Native Settings Tab order/Escape and notification focus/Escape were observed
  using the unsaved new-party form. Retain the component evidence and complete
  the remaining manual routes against an agreed fixture when playing this card.

## Validation and Current State

Draft created 2026-11-17; closeout review inputs added the same day. No additional
implementation or full route/assistive-technology audit performed here.
Follow Testmints for behavioral tests. Prefer component/router tests for local
navigation; use E2E when a contract depends on assembled routes/auth/services.
Run focused checks and full `fish -lc './gradlew check --no-daemon'` outside the
sandbox for implementation slices. Review tests and production design separately,
commit passing slices together, and record validation gaps without claiming
unperformed manual checks. No push/release authorization is implied.
