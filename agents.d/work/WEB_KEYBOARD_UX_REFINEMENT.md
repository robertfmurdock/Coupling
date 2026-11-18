# Restore familiar visuals and natural keyboard order

## Goal and Constraints
Address feedback after WEB_BEST_PRACTICES_REVIEW_REMEDIATION. Compare previous
source and rendered deployed UI before edits; retain native links, buttons,
dialog dismissal and visible focus. Coordinate order through composition, never
positive tabindex values in leaf controls. No saved party data changes.

## Safe Slice Sequence
- [x] Restore notification trigger scale/border and news-first modal with shared
      CloseButton; component presentation regression checks plus existing modal
      interaction tests. Independently restores familiar presentation.
- [ ] Put party navigation before utilities in ConfigHeader and Spin before Copy
      through the owning pairing composition. Assembled component keyboard tests
      should fail on current order. Preserve all controls and screenshot boundary.
- [ ] Remove ineffective shell skip link, which targets a main wrapping the entire
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
2026-11-18: starting at c04b8bc8e, previously clean tree. No push/release authorized.
Next: notification presentation regression tests.

## Validation
Slice 1: both presentation tests red on the intended assertions (13.33px vs
inherited 32px; redundant heading before supplied heading). Component suite green
(35 tests), full `fish -lc './gradlew check --no-daemon'` green. Reviewed tests for
observable rendering and production for preserved native modal behavior; diff
whitespace check clean. Also compared original/current party-page seen and unseen
icons before production edits. Revised visual walkthrough pending.
