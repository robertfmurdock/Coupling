# Keyboard-accessible contributor actions

## Goal and Constraints

Allow keyboard users to open and dismiss contributor actions with predictable
focus, retaining the current popup artwork and anchored placement. Finding from
[accessibility audit](../work_completed/WEB_ACCESSIBILITY_AUDIT_FOLLOWUP.md). Priority: inaccessible
actions. Agree modal versus nonmodal interaction before implementation; this draft
does not prescribe a modal or permission to reposition existing UI.

## Evidence and Starting Points

ContributionCardHeader wraps participant PlayerCard in an onClick span with no
focusability or keyboard semantics. ContributionPopUpMenu opens a CouplingPopUp
before its child trigger in DOM order and has no explicit focus-entry, Escape or
return-focus contract. Existing ContributionPopUpMenuTest uses an artificial button
trigger: it proves popup content, close-click and player-list reset, but does not
exercise the actual participant trigger. Reproduce at the real composition boundary
before implementation; source evidence alone is not a calibrated AT result.

## Safe Slice Sequence

- [x] Add regression at the actual ContributionCardHeader/overview boundary for
  keyboard activation and the participant's visible accessible name. Keep detailed
  popup behavior in its existing component suite; do not hide the real trigger
  problem behind the test's artificial button.
- [x] Deliver named native triggers without duplicate nested controls or visual
  changes; compare before/after. Reuse menu callbacks and save commands.
- [x] Specify and test entry, Escape, Close and return-focus behavior, including
  player-list refresh/removal of the original trigger. Use the existing jsdom suite
  unless native popover/dialog/focus enforcement requires real Chrome. Retain a
  fallback if introducing a platform feature outside the supported baseline.
- [ ] Review populated contribution overview/list in native Chrome and calibrated
  Safari/VoiceOver; compare artwork/anchoring and check both known/unknown
  participant actions. Retain the card until that evidence is recorded.

## Completion and Current State

Drafted 2026-11-19. No implementation or manual reproduction yet. Run focused and
full checks for each implementation slice; complete native keyboard and calibrated
Safari/VoiceOver review before closing. No push/release authorization. Define how
participant actions work when there is no onPlayerClick handler before playing.


## Active Play: 2026-11-19

Starting at b9abe6091. First independently releasable slice: fix the real
ContributionCardHeader participant trigger while preserving popup modality,
placement and callbacks. Handler-bearing cards become named native non-submit
buttons; cards without handlers remain static. Popup focus/dismissal is a separate
slice; no new overlay model or visible control placement is selected here.

Regression in components/jsdom renders the real header with Alex/Sam, uses
simulated Tab/Enter/Space and verifies the selected player and actual trigger
anchor. It failed because no Alex button existed. The no-handler scenario checks
static content. The module needs a small typed user-event keyboard binding, matching
the established client test binding; this is simulated keyboard evidence, not a
native browser or screen-reader result. Production preserves player artwork and
resets native button chrome without removing focus outlines. Focused/full results
and final review follow before commit.


Trigger slice validation: components/jsdom 130 tests pass. Real Chrome layout/focus
scenario compares action-bearing cards with static cards at equal test-owned host
widths: matching card dimensions/relative position and visible focus outline.
An initial fixture shadowing error was corrected before product assertions ran.
Chrome's asset stubs do not establish artwork loading or visual screenshot review;
the geometry and computed-focus claims are bounded accordingly. Focused Chrome
50 tests and formatting pass. Full `fish -lc './gradlew check --no-daemon'`
passes (680 tasks); log `/tmp/coupling-contributor-trigger-check.log`.
Test review confirms actual header coverage, callback anchor and no nested focus
target; production review confirms opt-in button semantics with unchanged card
styles/placement and no handler means static content. Diff check clean.

Owner agreed the next popup slice should remain nonmodal with anchored appearance,
focus entry, Escape/Close return, and Tab leaving freely. No focus trap or dimming.
Native keyboard/visual and calibrated Safari/VoiceOver review remain required for
card completion. No push/release performed.


## Nonmodal Focus Slice: 2026-11-19

Previous completed slice: `76d930633`. Owner selected the nonmodal design. Preserve
Popper's existing anchored shell/arrow and appearance; do not introduce a focus
trap, backdrop or native top-layer positioning change in this slice. The existing
popup component owns focus entry and dismissal. Its content is a named nonmodal
dialog, focus enters Close, explicit Close/Escape restores a connected trigger,
and blur out of the popup closes without pulling focus back. Player refresh closes
it and returns focus only when it held focus and the trigger survives; a removed
trigger is not focused (document fallback remains).

Four original regressions failed for missing entry, return, Escape dismissal and
Tab-leave behavior before production edits. Existing refresh test was vacuous:
it checked absence of Create Player although that fixture showed Player Config.
It now checks the actual content and popup removal, with separate surviving and
removed trigger cases. Prerequisite render/open moved to setup; rerender is the
exercise. The shared TestRouter remounts on rerender by recreating its router, so
this suite uses a typed test-only MemoryRouter export with stable provider identity
instead. The legacy Kotlin binding does not expose that installed React Router
export; no production router changes are needed.

Native Chrome reached the local populated-player party's Contributions route,
but it shows onboarding rather than contribution records. No popup visual/native
keyboard pass is claimed, and no contributions/secrets/integrations were created.
Need a contribution-bearing local fixture for final native/VoiceOver review.
Focused/full results and final review follow before commit.


Focus slice focused validation: 133 component tests and formatting pass. The
surviving-trigger fixture also needed an explicit stable key, matching the real
header's participant-ID key; otherwise React replaced the button and correctly
prevented return to a detached node. The test asserts trigger identity survives
the update as well as final focus, so it cannot silently become the removal case.
Temporary diagnostic output removed. Test review retains six distinct popup
contracts and real-trigger tests in ContributionCardHeaderTest; production review
confirms focus is owned locally, Tab is not trapped, and Popper options and styles
are unchanged. Full-suite gate follows before commit. No push/release performed.


Full `fish -lc './gradlew check --no-daemon'` passed (680 tasks); log
`/tmp/coupling-contributor-focus-check.log`. `git diff --check` clean. No temporary
diagnostics remain. This completes the automated focus/dismissal slice following
`76d930633`; no push/release. Remaining card work: contribution-bearing local
fixture, native Chrome artwork/anchoring and keyboard walkthrough, then calibrated
Safari/VoiceOver review. The local browser currently shows the empty Contributions
onboarding page; existing player/new-party tabs were preserved. Do not mark the
card complete from jsdom and computed-style/geometry checks alone.


## Live Fixture and Unknown-Participant Anchor Regression: 2026-11-19

Continued at `563e7143a`. Created a separate local party through the logged-in
GraphiQL UI, without changing existing parties or creating secrets/integrations:
`accessibility-contributor-review-20261119` (“Disposable accessibility review”).
One contribution (`accessibility-review-20261119`, label “Keyboard review”) has
fictional `alex@example.invalid` and `sam@example.invalid` participants. Retain
this named fixture for owner review; no cleanup/deletion performed.

Fresh native Chrome at 100%, 1200x953 window: Tab reaches alex with visible focus;
Enter opens actions and focuses Close, Tab reaches Create Player. But the popup
appeared at the viewport's top-left instead of its participant, and Escape left
focus on the page. Unknown participants receive new generated IDs on each header
render; using that ID as the React key detached the selected reference node.

Added a components/jsdom composition regression using real ContributionCardHeader
inside ContributionPopUpMenu. It failed because opening replaced the original
button. Smallest correction: key participants by their first matching contribution
email, preserving known-player alias deduplication and the same key when an unknown
participant is saved. Generated player IDs/save commands and artwork/styles are
unchanged. No memoization cache or new overlay positioning is introduced.
Focused/full validation and native recheck follow before commit.


Anchor slice validation: component tests (134) and formatting pass; full
`fish -lc './gradlew check --no-daemon'` passes (680 tasks), log
`/tmp/coupling-contributor-anchor-check.log`. Test review confirms actual header +
popup composition and connected-node identity, not only an artificial trigger.
Production review confirms the sole behavior change is stable participant keys;
existing alias matching and player generation/save data stay intact. Diff clean.

Fresh native Chrome after hard reload: unknown-participant popup is now anchored
beside its card rather than at viewport origin. Enter/Space opens, Close receives
focus, Escape and pointer Close return to the participant; Tab reaches actions and
leaves without trapping. Overview and List both show the existing anchored artwork
for known alex and unknown sam. Player Config activates by native Tab/Enter and
loads alex's form. Created fixture alex using the popup's Create Player action;
fictional email only. Sam remains unknown so both branches remain reviewable.
Fixture URL: https://localhost/accessibility-contributor-review-20261119/contributions/list

One residual live finding: after Create Player saves and refreshes the page data,
focus falls to the page instead of returning to the participant. Reopening and
explicit dismissal work. Investigate the assembled refresh/unmount path as the
next slice; the card stays open. Safari/VoiceOver still requires owner validation.
No push/release or fixture deletion performed.
