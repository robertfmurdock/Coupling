# CPL-61 — Readable shared form labels and hints

## Goal and Constraints

Slice of [CPL-56](../work/CPL-56-WEB_ACCESSIBLE_DATA_AND_REFLOW_FOLLOWUP.md). Raise
shared Editor label/hint text contrast to at least 4.5:1 on existing backgrounds.
Preserve layout, text sizes, borders, background colors and form behavior. This is
not a general palette, state-contrast or forced-colors audit.

## Safe Slice Sequence

- [x] Reproduce insufficient contrast using computed styles in real Chrome.
- [x] Prepare darker gold/brown; run focused/full checks and present comparison.
- [x] Record final owner approval; review test/implementation and close.

## Design and Review

Rendered Chrome component regression failed at 2.3097:1 for the Name label. The
fixture uses actual Editor/configInput plus the existing hint span structure.
It reads computed opaque RGB foreground/background values and applies WCAG sRGB
relative luminance. Both normal-text pairs must reach 4.5:1 without pinning the
test to the selected gold. A real-browser component style test owns this contract;
jsdom/E2E is unnecessary for this computed-color assertion.

Implementation changes only the two Editor text colors from #CDA018 to #856000.
The original pairs calculate to 2.31:1 and 2.19:1; the candidate to 5.44:1 and
5.16:1. Bright gold borders and cream backgrounds remain. Test review confirms
both label/hint coverage and the production styling composition. Production review
confirms no size, spacing, border, background, layout or behavior changes.
Modern Web Guidance color: “Maintain 4.5:1 for normal text.”

## Owner Review and Visual Evidence

Captured a valid original full New Party screenshot. Post-rebuild local page
rendering remained blank, then native access reported noWindowsAvailable; no
full-page candidate visual pass is claimed. Presented an explicitly labelled
isolated before/candidate comparison in native Chrome at
`/tmp/coupling-editor-contrast-comparison.html`, using Editor styling/text.
The owner approved: “it looks fine to me,” while emphasizing the playful game-show,
eclectic Price Is Right-style palette and preserving its energy. Recorded that
objective in the authoritative production style guide at the owner's request.
The full-page contextual visual check remains a recorded validation gap; owner
acceptance is of the sample, not a claimed complete runtime contrast audit.

## Validation and Completion

Focused browser suite (52 tests), formatting and full
`fish -lc './gradlew check --no-daemon'` pass (690 tasks); log
`/tmp/coupling-editor-contrast-check.log`. Diff checked. Only documentation changed
after that successful implementation gate. Closed with bounded owner visual
acceptance and the contextual-browser gap above. No form data saved, push or release
performed. Broader accessibility work remains on CPL-56.
