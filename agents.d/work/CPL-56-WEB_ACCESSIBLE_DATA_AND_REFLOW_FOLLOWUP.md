# CPL-56 — Accessible data views, reflow and contrast

## Goal and Constraints

Provide usable nonvisual data access and responsive presentation for statistics,
contributions and shared forms. Follow-up to the bounded
[accessibility audit](../work_completed/CPL-53-WEB_ACCESSIBILITY_AUDIT_FOLLOWUP.md). Preserve established
controls; seek user approval for color, layout, or new visible data alternatives.
This draft groups review inputs; split independently designed deliverables before
playing rather than treating every chart and page as one implementation slice.

## Evidence and Candidates

- Heatmap.kt writes numeric data only to background colors on generated divs.
  PlayerHeatmap supplies row/column player cards but no semantic cell relationships
  or textual numeric values. PairReportTable is a visual card list, not an HTML
  table; do not simply add table roles to its unrelated div layout.
- CouplingResponsiveLine has pointer legend toggles. Determine library-provided
  keyboard/AT support at runtime before asserting none exists. Compare text/data
  alternatives with the plotted metric; existing spin counts are not automatically
  equivalent to all chart series.
- Statistics and contribution views contain fixed 600px plots and nowrap/inline
  containers. Native narrow-width/zoom walkthroughs must establish which controls
  or data become unreachable before selecting a responsive design.
- Shared Editor label/hint contrast is complete in [CPL-61](../work_completed/CPL-61-EDITOR_TEXT_CONTRAST.md).
  Owner accepted the before/candidate sample; the full-page candidate visual pass
  was blocked by local blank rendering/browser access. Other contrast/states and
  forced-colors remain outside that bounded slice.
- GeneralControlBar heading correction is split into [shared header semantics](../work_completed/CPL-60-WEB_GENERAL_HEADER_SEMANTICS_FOLLOWUP.md);
  keep its implementation and native before/after evidence on that bounded card.
- Route-title correction is split into [distinct browser titles](../work_completed/CPL-59-WEB_ROUTE_TITLES_FOLLOWUP.md);
  keep its implementation and navigation evidence on that bounded card.
- Shared query loading converts failures to null, sharing not-found recovery UI.
  Distinguishing network failure/authorization/missing data changes the state model;
  define ownership and recovery contracts before implementing announcements.

## Safe Slice Sequence

- [ ] Calibrate Chrome keyboard, Safari/VoiceOver, 320px reflow, 200% zoom and
  forced-colors/contrast conditions. Record populated fixtures and exact browser,
  dimensions, colors and results; do not equate macOS Increase Contrast with
  forced-colors. Reuse audit evidence where conditions match.
- [ ] Select one metric/view, design its meaningful data alternative and obtain
  approval for visual changes. Test data/labels/actions at the owning component;
  use browser checks for geometry, styles or platform interaction.
- [ ] Deliver measured contrast and reflow fixes in separate releasable slices,
  preserving desktop placement unless approved; do not mask overflow by clipping.
- [ ] Recheck data interpretation with AT, keyboard operation and visual review;
  retain any untested library/integration conditions explicitly.

## Completion and Current State

Drafted 2026-11-19. Page-title, shared-header semantics and Editor text-contrast slices are complete
in the linked cards. Remaining findings are review candidates; the broader
runtime/AT matrix is not complete. The approved About-only reflow correction stays on the
original audit card; do not transfer that unfinished work here. Each slice requires
focused checks, full ./gradlew check and a passing test-plus-implementation commit.
No push/release authorization. Refine the route/metric set and stopping point first.


## Audit Closeout Handoff: 2026-11-19

The bounded parent audit closed with owner acceptance; its full calibrated matrix
was not completed. Use the [remaining manual checklist](../research/WEB_ACCESSIBILITY_MANUAL_REVIEW.md)
when defining this card's scope. Record browser/OS versions, VoiceOver calibration,
populated saved-route coverage, runtime contrast and forced-colors conditions;
route integration/status checks must not connect real accounts or create secrets.
Pairing and contributor-specific checks stay with their respective follow-ups.
Split general route/state/AT deliverables into further cards if needed before play.

Native Chrome showed horizontal Party List overflow at 200% zoom; reproduce with
exact dimensions before choosing a correction. New-party card navigation is fixed
in `421fe94ca`, but its other party-specific navigation still targets unsaved IDs;
review the intended pre-save navigation/state contract. Existing About reflow is
complete and must not be reclassified as unfinished work here. Owner acceptance
is not measured contrast, forced-colors, or calibrated screen-reader evidence.
