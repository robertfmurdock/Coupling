# Keyboard-accessible contributor actions

## Goal and Constraints

Allow keyboard users to open and dismiss contributor actions with predictable
focus, retaining the current popup artwork and anchored placement. Finding from
[accessibility audit](WEB_ACCESSIBILITY_AUDIT_FOLLOWUP.md). Priority: inaccessible
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

- [ ] Add regression at the actual ContributionCardHeader/overview boundary for
  keyboard activation and the participant's visible accessible name. Keep detailed
  popup behavior in its existing component suite; do not hide the real trigger
  problem behind the test's artificial button.
- [ ] Deliver named native triggers without duplicate nested controls or visual
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
