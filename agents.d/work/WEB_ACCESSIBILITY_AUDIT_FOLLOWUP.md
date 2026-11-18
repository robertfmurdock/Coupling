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

## Continued Play: Player Badge Names

2026-11-18, following layout correction f38844854. Player Configuration's badge
options displayed party-configured names but exposed fixed accessible names
"Default Badge Option" and "Alt Badge Option". A component regression with
"Navigator" and "Driver" reproduced the mismatch. The native options now contain
the configured names as text, retaining their Default/Alternate values, ordering,
and appearance. Existing E2E selection/save coverage uses those visible names.

Removing only the ARIA overrides left the names solely in option `label`
attributes, which this Testing Library version reports as empty accessible names.
Using native option text avoids both the mismatched override and that test-library
limitation. This does not establish a calibrated screen-reader result.

Focused `:client:jsBrowserTest` passed (46 tests); formatting passed. Full
`fish -lc './gradlew check --no-daemon'` passed (680 tasks; 61 E2E tests, zero
failures). Test review confirmed custom names map to the original values and
existing selection/save scenarios remain covered. Production review confirmed
unchanged styles, placement, values, and handlers. Diff check clean. Recorded in
this badge-name slice commit; remaining inventory and manual matrix stay open.

## Continued Play: Email Access Instructions

2026-11-18, following badge-name slice 08ee09bbf. The existing player Email
instructions explain that the address grants party access, but were not associated
with the field. A rendered component query by name and description reproduced the
missing accessible description. Added optional `describedBy` support to the shared
`configInput` helper and linked only the two existing access-instruction lines.
The separate Gravatar link remains outside the description. Text, styles, DOM
order, focus behavior, field value, and change/save handlers are unchanged.

The test query required a separate typed options binding because the dependency's
generated `RoleOptions` cannot be extended. After resolving that harness issue,
the test failed on the missing Email description, then passed with the association.
Focused `:client:jsBrowserTest` and formatting passed. Full
`fish -lc './gradlew check --no-daemon'` passed (680 tasks; 61 E2E tests, zero
failures). Test review confirmed the description is computed from the existing
instructions; production review confirmed an opt-in shared helper with unchanged
layout and handlers. Diff check clean. Recorded in this email-description slice
commit. This is computed-description evidence, not a calibrated screen-reader
announcement check. The remaining inventory and manual matrix stay open.

## Testmints Review and Test-Level Consolidation

Reviewed recent naming/description, layout, focus, Settings, Demo, keyboard-order,
and badge persistence tests. Rendering-only player names, badge names, and Email
descriptions do not need Chrome: moved four scenarios to the existing components
jsdom suite with synchronous Testmints phases and the existing router wrapper.
Kept roster and compact-roster scenarios separate because they exercise distinct
composition paths. Kept Email and badge contracts separate; shared render boilerplate
does not justify hiding their subject calls behind a generic test factory.

Existing default/alternate badge component tests asserted only that options existed.
They now assert the selected value and have names matching that evidence. Temporarily
forcing the dropdown to Default produced the intended alternate-selection failure;
restoring production made all 122 component scenarios pass. No production diff
remains. Removed two redundant badge rendering/initial-selection E2E scenarios;
retained assembled save-and-reload persistence coverage and moved reload into
`exercise` so `verify` only observes the persisted selection.

Layout, computed focus styling, native dialog, and inert behavior retain real-browser
evidence. Router/user-event tests are already component tests, not E2E; simulated
keyboard input remains distinct from native Tab evidence. Further jsdom migration
is possible for browser-independent interaction cases, but no new runner/harness
is introduced by this consolidation. Formatting and focused components checks
passed. Full `fish -lc './gradlew check --no-daemon'` passed (680 tasks; 59 E2E
scenarios, zero failures). Final review confirmed unchanged production and retained
contract coverage; diff check clean. Recorded in this test-consolidation commit.

## About Navigation and News Popup Feedback

2026-11-18: user reported awkward About Back-link positioning, vertical news-popup
clipping at some window sizes, and missing Close hover feedback. Comparison with
10689abe8's parent confirmed the About link retained its old absolute-positioned
wrapper. A Chrome geometry regression reproduced the lack of reserved space above
the article. The wrapper is now a right-aligned normal-flow row, keeping the link
above the article and preserving its destination and single focus stop.

The shared Close button now has a pointer cursor and subtle rounded hover background
without changing dimensions or dismissal behavior. Existing interaction checks
remain in place; native visual review is separate from simulated user-event checks.

The news dialog already has `max-height: 80vh` and `overflow-y: auto`. New real-Chrome
geometry checks pass for scrolling to the end of a long post at a short height and
for a long URL at narrow width; no overflow production change is claimed. Native
Chrome at 150% zoom also reached the actual current post's final “– RoB” line via
scrolling. User confirmed the reported clipping was vertical; browser/window/zoom
details requested to reproduce that remaining case. Do not mark the report fixed
based only on the passing Chrome cases.

Focused Chrome checks and formatting passed. Full
`fish -lc './gradlew check --no-daemon'` passed (690 tasks; 59 E2E tests, zero
failures). Native Chrome review of the fresh local build confirmed the Back link
above the article, the Close hover highlight, and Escape returning focus to the
info trigger. Browser zoom restored to 100%. Test review distinguished the failing
About geometry regression from already-passing overflow evidence; production
review confirmed only the requested Back placement and shared Close styling changed.
Diff check clean. Recorded in this UI-correction slice commit. Clipping reproduction
remains open alongside the broader audit matrix.

## Follow-up Visual Refinement

User preferred Back beside the About title instead of its own row, and disliked
the square scrollbar at the news dialog's rounded edge. This supersedes the
above-article Back placement in e7f6b0734. About's shared header now owns its single
`h1` and Back link in one wrapping, vertically centered flex row. The Markdown body
starts at the existing Welcome subheading, avoiding a duplicate title.

News scrolling moves from the outer dialog to the existing inset content wrapper.
Only an open dialog becomes a flex column; the inner area can shrink and scroll
within the height bound while the outer shell retains its rounded edge. Native
scrollbars and their default hit-target size are preserved. No custom scrollbar
widget, global body scroll lock, new focus stop, or changed Close placement.

Both revised Chrome regressions failed before implementation, then passed: peer
header alignment and inset scrolling to the last paragraph. Narrow overflow checks
cover the inner scroll area as well as the shell, so clipping cannot masquerade
as successful wrapping. That stronger check caught horizontal overflow inside the
new narrower scroll area; `overflow-wrap: anywhere` at the content owner resolved
it. All focused Chrome tests and formatting pass. Full
`fish -lc './gradlew check --no-daemon'` passed (680 tasks; 59 E2E scenarios, zero
failures). Test review confirmed the selected alignment, inset scrolling, final
paragraph reachability, and inner overflow assertions. Production review confirmed
one About title owner, native scrolling, and display styling scoped to open dialogs.
Diff check clean. Recorded in this visual-refinement commit.

Fresh manual visual review could not be completed: the native browser connection
returned inconsistent AX/screenshot state, then `noWindowsAvailable`; browser-host
inventory also reported unavailable. Do not treat those attempts as visual evidence
for this refinement. Automated Chrome geometry evidence passes. The previously
reported browser/window-specific vertical clipping case remains unreproduced.
