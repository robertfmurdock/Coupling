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
- [ ] Specify and test entry, Escape, Close and return-focus behavior, including
  player-list refresh/removal of the original trigger. Use the existing jsdom suite
  unless native popover/dialog/focus enforcement requires real Chrome. Retain a
  fallback if introducing a platform feature outside the supported baseline.

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
