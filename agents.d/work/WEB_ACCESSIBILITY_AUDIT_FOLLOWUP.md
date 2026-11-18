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

- The ineffective shell skip link was removed by WEB_KEYBOARD_UX_REFINEMENT:
  its target wrapped the entire React mount, including navigation. The main
  landmark remains. Consider a meaningful bypass only where repeated navigation
  and a distinct content target make it useful; sparse Welcome screens need no
  artificial extra focus stop.
- Demo illustration focus leak fixed in 1e3bbf33d: application frames are inert,
  introductory content and real return links remain outside that restriction.
  Still verify spoken explanations and animation timing in calibrated AT.
- Shared player portraits repeated generic image text before visible names.
  Roster naming regression and decorative portrait fix recorded in active play.
- Shared data loading collapses failed/null results into the same recovery UI.
  GraphiQL has a distinct authorization loader. Review error distinctions and
  calibrated announcements separately before claiming all errors are covered.
- Original closeout did not manually visit saved-party/player/pin routes: the
  local signed-in account had no parties, and no new data was saved for review.
  Native Settings Tab order/Escape and notification focus/Escape were observed
  using the unsaved new-party form. Retain the component evidence and complete
  the remaining manual routes against an agreed fixture when playing this card.

## Validation and Current State

Draft created 2026-11-17; active implementation began 2026-11-18. See active-play
evidence below. A full route/assistive-technology audit is not yet complete.
Follow Testmints for behavioral tests. Prefer component/router tests for local
navigation; use E2E when a contract depends on assembled routes/auth/services.
Run focused checks and full `fish -lc './gradlew check --no-daemon'` outside the
sandbox for implementation slices. Review tests and production design separately,
commit passing slices together, and record validation gaps without claiming
unperformed manual checks. No push/release authorization is implied.

## Active Play: 2026-11-18

Starting at e9d85a535 with a clean tree. Begin with the concrete residual
findings; do not claim a calibrated full-route audit from component checks.

1. Demo boundary: reproduce native programmatic focus entering an illustrated
   form; make only illustrated content inert, retaining explanatory popup and
   real return navigation. Verify final-frame return-link usability separately.
   This is independently useful without other audit changes.
2. Inventory portrait ownership and names; fix redundant/generic alternatives at
   the narrowest owner only after tests capture the actual link/card contract.
3. Record route/control evidence and prioritize remaining audit work, including
   explicit browser/screen-reader/zoom gaps. Do not mark the broad audit complete
   while required manual evidence is missing.

Demo slice complete: native programmatic-focus regression red (input took focus),
then green with inert illustrated application frames. Introduction return-link
regression also red under over-broad inert, then green with introduction states
excluded. Final-frame return remains focusable outside the inert subtree. These
are native DOM focus calls in real Chrome component tests, not a native Tab or
screen-reader walkthrough. Three Demo tests pass. Test-only Karma asset mapping
resolves real processed Demo images instead of placeholder modules.

Final full `fish -lc './gradlew check --no-daemon'` green: 680 tasks, 61 E2E
scenarios with no failures. Test review checked focus outcomes and real return
navigation; production review checked shared ownership, unchanged visuals, and
preserved introductory content. `git diff --check` clean. Commit: 1e3bbf33d.
Portrait slice: both roster regressions failed with accessible link name
"player-icon Alex" instead of "Alex", then passed with the redundant PlayerCard
portrait alt set to empty. Visible name/image sources/styles unchanged. Existing
Welcome image-selection checks still assert the same source URLs. The spin E2E
image locator now selects img within its existing player-card scope.

Focused `:client:jsBrowserTest :client:components:jsTest` green; full
`fish -lc './gradlew check --no-daemon'` green (680 tasks; 61 E2E tests, zero
failures). Reviewed tests for exact accessible names and retained artwork/click
contracts; reviewed production for a single shared decorative-image owner.
Diff check clean. Commit: this portrait slice.

Handoff: no uncommitted implementation expected after this slice; no push or
release. Remaining work is the manual matrix and deeper route/control checks
below. Keep this card open until that evidence is obtained; no calibrated
screen-reader, contrast or zoom/reflow result is claimed by this play.

## Bounded Inventory and Remaining Evidence

Inventory source: CouplingRouter, CouplingRoute, DemoPageFrame, PlayerCard,
PlayerRoster, TinyPlayerList, and CouplingQueryLoadState. This table distinguishes
source/component evidence from unperformed route walkthroughs. Use disposable
local fixtures from existing test setup; no production writes are needed.

| Route/control boundary | Evidence available | Remaining audit |
| --- | --- | --- |
| `/welcome/`, `/about` | Existing heading, navigation, focus and reduced-motion component checks | Spoken outline, zoom and contrast |
| `/demo` introduction, forms, final return | Native DOM focus regression tests; real frame data and images | Native Tab, spoken descriptions, timing/pause usability |
| `/parties/`, `/user` | Shared header order tests from keyboard UX card | Saved-account form labels/errors and zoom |
| `/:partyId/pairAssignments/current/`, `/prepare/` | Shared roster names, Spin/Copy order, existing spin E2E scenarios | Keyboard alternative to drag/drop, announcements, zoom |
| `/:partyId/edit/`, `/player/new`, `/player/:playerId/`, `/pin/new`, `/pin/:pinId/` | Shared cards/header source inspected; existing form E2E coverage | Explicit labels, hints, validation/error recovery and spoken state per form |
| `/:partyId/history`, `/statistics`, `/contributions*` | Route inventory only | Table/header relationships, charts, filtering, keyboard interaction |
| `/graphiql/`, integrations/callbacks | Distinct auth/loading owners identified | Frame title/keyboard reachability and integration status announcements |
| Shared pending/null/error boundary | CouplingQueryLoadState handles pending and null results | Distinguish network failure, missing data and recovery announcements before redesign |

Working priority rubric: first fix unintended navigation/editing or unreachable
core actions; then misleading names/roles; then redundant announcements. Treat
unmeasured contrast/reflow and screen-reader behavior as unknown, not passing.
Current verified fixes: Demo focus containment (core interaction) and roster link
names (redundant announcements). Do not infer AT results from DOM focus or role
queries.

Manual matrix still to execute: Chrome keyboard at normal and 200% zoom, narrow
viewport reflow, forced colors where supported, and Safari with VoiceOver using
standard bindings. Screen-reader calibration and fixture coverage must be recorded
before completing the broad audit. No screen-reader certification is claimed.
