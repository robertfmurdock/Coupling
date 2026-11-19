# Accessible data views, reflow and contrast

## Goal and Constraints

Provide usable nonvisual data access and responsive presentation for statistics,
contributions and shared forms. Follow-up to the bounded
[accessibility audit](WEB_ACCESSIBILITY_AUDIT_FOLLOWUP.md). Preserve established
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
- Editor declares 14px gold #CDA018 labels on #FDF9ED and 11px hints on #FBF3DA.
  WCAG sRGB contrast calculation for those source pairs gives 2.31:1 and 2.19:1,
  below the 4.5:1 normal-text criterion. Confirm computed runtime colors, states and
  backgrounds; source calculation does not establish a complete contrast audit.
- GeneralControlBar's h1 includes navigation controls; validate spoken outline and
  scope a semantic-only correction with before/after visuals if confusing.
- CouplingRouter labels integrations as Pin List and gives all three contribution
  views the same title. Validate rendered document titles and choose distinct names.
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

Drafted 2026-11-19. Source findings and candidates only; no implementation or
passing runtime matrix. The approved About-only reflow correction stays on the
original audit card; do not transfer that unfinished work here. Each slice requires
focused checks, full ./gradlew check and a passing test-plus-implementation commit.
No push/release authorization. Refine the route/metric set and stopping point first.
