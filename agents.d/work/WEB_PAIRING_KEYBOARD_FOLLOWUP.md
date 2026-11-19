# Keyboard alternatives for manual pairing and pins

## Goal and Constraints

Make manual player rearrangement and pin assignment usable without drag gestures.
Finding from [accessibility audit](../work_completed/WEB_ACCESSIBILITY_AUDIT_FOLLOWUP.md).
Priority: core keyboard capability. This is a draft; agree the visible interaction
before implementation. Preserve pointer drag behavior and obtain approval before
adding/repositioning controls or changing the established pairing layout.

## Evidence and Starting Points

Source review: components/pairassignments/DraggableThing.kt registers drag/drop on
span/div refs with no keyboard interaction. Review PairSectionPanel and pin bag
call sites to enumerate move/swap/unpair/pin/remove operations before claiming all
manual paths are covered. This is not a recorded native-keyboard reproduction.
Spin itself remains a separately named, keyboard-operable control.

## Safe Slice Sequence

- [ ] Reproduce one representative unavailable operation with a local two-player,
  one-pin fixture and native keyboard; map the current callbacks and save behavior.
  Agree a minimal interaction that exposes equivalent operations without dragging.
- [ ] Deliver one complete operation at its owning component/action boundary;
  use jsdom for selection/action contracts and real Chrome for actual focus/style
  enforcement. Reuse existing mutation paths; keep save/reload E2E evidence only
  when persistence is part of the changed contract.
- [ ] Complete remaining distinct operations, including cancel/no-op behavior and
  stable focus after a move. Test relevant announcements with calibrated AT.

## Completion and Current State

Drafted 2026-11-19 from source audit. No implementation or passing manual evidence.
Finish with focused checks, full ./gradlew check, visual comparison and independent
safe commits. No push/release authorization. Define the precise operation inventory
and stopping point before playing; do not turn this into a general pairing redesign.
