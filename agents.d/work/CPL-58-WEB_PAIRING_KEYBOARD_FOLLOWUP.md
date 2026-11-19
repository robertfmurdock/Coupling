# CPL-58 — Keyboard alternatives for manual pairing and pins

**Work card protocol: 2**

## Goal and Constraints

Make manual pairing and pin assignment operable without dragging. Preserve pointer
drag behavior, the existing save path, and the keyboard-operable Spin control.
Agree the visible interaction before adding or repositioning controls.
[Extended accessibility audit](../work_completed/CPL-53-WEB_ACCESSIBILITY_AUDIT_FOLLOWUP.md).

## Acceptance Evidence

- A keyboard user can complete every supported manual operation. Inventory
  move, swap, unpair, assign pin, and remove pin against the existing callbacks.
- Cancel and invalid or no-op actions leave the arrangement intact. Focus stays
  on a predictable control after a move, and relevant state changes are announced
  in calibrated assistive technology.
- Pointer drag still performs the same operations. Verify save and reload for
  changed paths whose contract includes persistence.

## Safe Slice Sequence

1. Reproduce one unavailable operation in native Chrome using the two-player,
   one-pin fixture from the [manual checklist](../research/WEB_ACCESSIBILITY_MANUAL_REVIEW.md).
   Trace `DraggableThing.kt`, PairSectionPanel, and the pin bag to map operations
   and callbacks. Agree the interaction and exact operation inventory.
2. Ship one operation at its owning component and action boundary. A keyboard
   scenario should initially fail to invoke that operation. Verify the action
   contract in jsdom and actual reachability and focus in Chrome. Reuse the
   existing action so the result is useful with pointer drag still available.
3. Deliver the remaining operations in complete slices using the same
   interaction. Check cancel, stable focus, and announcements with calibrated
   assistive technology.

## Evidence

Source review found drag/drop registration on span/div refs without keyboard
handlers. Native reproduction and implementation are pending.
