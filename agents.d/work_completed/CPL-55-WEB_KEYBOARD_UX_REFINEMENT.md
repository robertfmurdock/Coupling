# CPL-55 — Restore familiar visuals and natural keyboard order

## Goal and Constraints
Address feedback after WEB_BEST_PRACTICES_REVIEW_REMEDIATION. Compare previous
source and rendered deployed UI before edits; retain native links, buttons,
dialog dismissal and visible focus. Coordinate order through composition, never
positive tabindex values in leaf controls. No saved party data changes.

## Safe Slice Sequence
- [x] Restore notification trigger scale/border and news-first modal with shared
      CloseButton; component presentation regression checks plus existing modal
      interaction tests. Independently restores familiar presentation.
- [x] Put party navigation before utilities in ConfigHeader and Spin before Copy
      through the owning pairing composition. Assembled component keyboard tests
      should fail on current order. Preserve all controls and screenshot boundary.
- [x] Remove ineffective shell skip link, which targets a main wrapping the entire
      application including navigation. Retain main landmark; reassess meaningful
      bypass navigation separately if future page structure needs it.

## Design Decisions
Compared pre-card NotificationButton source (4ae0f09c9^) and deployed UI with local
HEAD before edits. Deployed Brave and local Chrome have different window/account
fixtures; both use Nov 16 news. Observed small native-button icon/border locally,
and redundant Recent information heading/raw Close above News in current modal.
Keep native dialog and bounded scrolling, restore 5px outer padding, use shared
close affordance beside news rather than adding another content heading.

## Current State
Completed 2026-11-18. Commits: b3e1901bb (notification presentation), ffafa026a
(shared keyboard composition), and this closeout (shell link removal).
No push/release performed. Extended screen-reader and route audit remains in the
separate WEB_ACCESSIBILITY_AUDIT_FOLLOWUP card.

## Validation
Slice 1: both presentation tests red on the intended assertions (13.33px vs
inherited 32px; redundant heading before supplied heading). Component suite green
(35 tests), full `fish -lc './gradlew check --no-daemon'` green. Reviewed tests for
observable rendering and production for preserved native modal behavior; diff
whitespace check clean. Also compared original/current party-page seen and unseen
icons before production edits. Revised party-page icon and news-first dialog visually checked in Chrome.

Slice 2: component keyboard contracts failed on original party-header and Spin/Copy
order, then passed with DOM ordering owned by ConfigHeader, GeneralControlBar,
and PairSection. No positive tabindex. Full check caught a narrow-window click
interception by the info button; intrinsic header minimum width fixed it. Final
full check green (680 tasks; 61 E2E tests, zero failures). Reviewed tests separately
for observable focus order and production for composition ownership, unchanged
copy-image boundary, and optional notification use on shared headers. Diff check
clean. Chrome visually confirmed full-size party info icon and Spin beside Copy.
Native keyboard review was incomplete: automation returned delayed focus states;
component simulated-Tab evidence must not be described as native-key evidence.

Slice 3: mechanical removal of the ineffective link, unused target id/tabindex,
and CSS; main landmark and global focus-visible rules retained. No invented red
test for this deletion. Full check green (680 tasks; 61 E2E tests, zero failures).
Reviewed template/CSS and follow-up scope; no remaining shell references to
skip-link or main-content. Chrome reload confirmed the link absent from the
rendered accessibility tree. Native Escape returned focus from news to the info
button. A complete final native Tab walkthrough remains unverified because the
computer-use capture repeatedly returned stale states/errors; simulated component
keyboard checks are green. No saved party data changed.

Completion review: presentation retains native button/dialog semantics, layout
and order belong to page compositions, and clipboard image capture excludes the
new toolbar. Testing guidance was already added in c04b8bc8e after the original
card closed; no duplicate agent-context rules needed.
