# CPL-56 — Accessible data views, reflow and contrast

**Work card protocol: 2**

## Goal and Constraints

Make populated data views understandable without relying on chart colors and
keep shared views usable at narrow widths and zoom. Start with a reproducible
route or metric. Agree visible changes to layout, colors or data presentation
before implementation. [Extended accessibility audit](../work_completed/CPL-53-WEB_ACCESSIBILITY_AUDIT_FOLLOWUP.md).

## Acceptance Evidence

- Party List actions and content remain reachable at the reproduced 200% zoom
  condition. Record 320px reflow separately and preserve desktop placement.
- For the chosen chart, users can obtain the same metric, labels and values
  conveyed visually. Verify the alternative with populated data and calibrated
  assistive technology. Check any interactive legend in the actual browser.
- For any contrast fix, record the foreground/background pair and ratio and
  verify relevant states. Record forced-colors behavior in a supporting
  environment or explicit emulation.
- Record browser and OS versions, fixture state, viewport, zoom and untested
  conditions in the [manual checklist](../research/WEB_ACCESSIBILITY_MANUAL_REVIEW.md).
  Inspect integration and secret-related views without connecting real accounts
  or creating secrets.

## Safe Slice Sequence

1. Reproduce Party List horizontal overflow at 200% zoom with exact dimensions.
   If confirmed, add a browser regression that fails on unreachable content and
   fix the owning layout. Compare desktop placement. This can ship on its own.
2. Select one populated chart and agree its data alternative. Test the affected
   component for missing values or relationships, then verify interpretation and
   operation in a browser and with assistive technology. Each complete view can
   ship independently.
3. Measure remaining contrast, forced-colors and reflow candidates with the
   shared checklist. Scope each confirmed fix to the affected state or view.
   Record dispositions for the remaining route and state findings before closing.

## Evidence and Decisions

- Native Chrome showed Party List overflow at 200% zoom. Exact dimensions are pending.
- Heatmap encodes values in color and lacks semantic cell relationships.
  PairReportTable is a card list. CouplingResponsiveLine has pointer legend
  toggles and needs runtime keyboard and assistive-technology verification.
  Compare alternatives to the actual series, since spin counts may differ.
- Statistics and contributions contain fixed 600px plots and nowrap containers.
  Verify which controls or data become unreachable before changing layout.
- Shared query loading folds failures into the missing-data fallback. Defining
  distinct network, authorization and missing-data recovery needs a separate
  state contract. Some pre-save party navigation still targets unsaved IDs.
  The new-party card navigation fix is recorded in the audit at `421fe94ca`.
- The audit closed with owner acceptance and a partial manual matrix. About
  reflow, [route titles](../work_completed/CPL-59-WEB_ROUTE_TITLES_FOLLOWUP.md),
  [shared heading semantics](../work_completed/CPL-60-WEB_GENERAL_HEADER_SEMANTICS_FOLLOWUP.md),
  and [Editor text contrast](../work_completed/CPL-61-EDITOR_TEXT_CONTRAST.md)
  have their own completion evidence. Editor full-page visual verification was
  blocked. Pairing and contributor checks have their own cards.
