# CPL-24 — Annotate the pairing heatmap with an expected baseline

**Work card protocol: 2**

## Goal and scope

Help coaches compare actual recent pairing counts with an equal-rotation reference
on the Statistics page (`PlayerHeatmap`), including pairs that have never occurred.
The owner clarified on 2026-11-23 that this concerns non-contribution heatmaps.
Leave contribution counts, mob contribution credit, contribution filters and ease
charts unchanged. Preserve the existing grid placement and colors in slice 1.

## Provenance and corrected references

Created May 26, 2026 as `viz-enhance-pair-heatmap.md` in
[358b048c0](https://github.com/robertfmurdock/Coupling/commit/358b048c0bfd89a4f6c41a3b86506634f3e89917),
“docs(visualization): Add contribution visualization context and work cards,”
co-authored with Claude Sonnet 4.5. The batch introduced seven visualization
initiatives, including the contribution overview later delivered as CPL-23.
The recorded motivation was identifying under-rotation and repeated pairing by
comparing observed counts with an expected baseline. No originating conversation,
incident or user report has been established from repository evidence. The CPL-24
number was assigned November 19 in `bcdbede5b`.

The original card mixed rotation semantics with `PairContributionsHeatMap`,
`CouplingHeatmapTooltip` and fake contribution data. Those components count
contributions, including mob credit to contained pairs; they cannot establish
rotation counts or last-paired spins. That mismatch was present at creation.
The companion visualization reference also conflated the contribution heatmap
with pairing frequency. Actual owners are:

- `StatisticsPageQuery` and `StatisticsPage.kt`: existing recent pairing counts,
  last-paired spins and history; request saved-spin IDs through the same query.
- `RecentTimesPairedQuery`: counts exact two-player pairs in the most recent five
  full rotation cycles, using the current active team's rotation period. Solos
  and mobs have no recent pairing count under this existing contract.
- `libraries/action/.../stats/heatmap`: shared window size and baseline calculation.
- `PartyStatistics` / `PlayerHeatmap`: Statistics composition and grid.

## Baseline contract

`min(saved spin count, spins per full rotation × 5) / spins per full rotation`.
Use floating-point division for short histories, and the same five-cycle constant
as the server's recent count. A missing history count or invalid rotation period
means unavailable, not zero. Zero saved spins has a zero baseline. Count saved
spin IDs at party scope rather than summing pair counts or deduplicating only
histories of pairs still present: those approaches can miss spins or double-count.

The reference assumes equal rotation across the current team. Absences, pins,
solo/mob work and changing membership can affect counts; it is not a quality score
or proof that the scheduling algorithm should have produced each pair that often.
Differences use the unrounded baseline; display one decimal place. Never label
missing pair data as a known zero. A known pair with no last-paired spin is shown
as never paired; a missing pair record is unavailable.

## Safe slice sequence

1. **Baseline and readable pair data** — one contributor owns query, shared
   calculation, Statistics composition and component. Add a baseline explanation,
   retain the existing color scale with an explicit legend, and offer an expandable
   semantic table of unique two-player pairs, recent count, expected count,
   difference and spins since last paired. Preserve the Recent Pairing chart and
   contribution views. This is independently useful before cell-level changes.
   Expected red: the Statistics composition has no baseline explanation. Test short
   history, five-cycle cap, empty history, unavailable data, invalid period,
   above/below/equal differences, known zero and last-paired states. Reuse Node/jsdom
   component tests and shared JVM/JS tests; browser evidence proves assembled data
   and actual rendering. Full `./gradlew check` and a complete-slice commit gate.
2. **Cell-level baseline annotations** — add actual/expected/difference and
   last-paired information to individual cells with equivalent keyboard access.
   Define a subtle baseline marker and color interpretation before changing colors.
   The original proposal simultaneously treated above-expected counts as good and
   aimed to identify over-pairing; resolve this contradiction rather than silently
   assigning a health judgment. The owner was unsure when asked on November 23;
   use neutral above/below descriptions and preserve the existing colors for now.
   The implementation choice is descriptive cell labels plus an inset outline for
   cells within one pairing of the exact baseline (the original ±1 tolerance),
   with a legend explaining that marker and the table as the keyboard alternative.
   Keep a numeric alternative and capture matched
   before/after states. Declare the owning UI contract and failing scenarios first.

## Acceptance evidence

- [x] Statistics shows the baseline for its actual saved-spin window and current
  rotation period; it does not mistake contribution counts for rotations.
- [x] Users can read unique-pair counts, expected values, differences and last-paired
  spins without relying on color or pointer hover. Unknown is distinct from zero.
- [x] Preserve existing grid colors/placement, Recent Pairing chart and contribution
  behavior in the first slice. Verify assembled short-history data in a browser.
- [x] Deliver cell-level annotations and a clearly explained baseline marker/color
  interpretation, or record explicit owner disposition of that remaining scope.
- [x] Record focused tests, full check, visual evidence and passing slice commits.

## Visual evidence

[Issue #3483](https://github.com/robertfmurdock/Coupling/issues/3483).
Before capture: `37d9b66b-91bc-4070-9d48-6d61ca6eadc7`, status complete,
Chrome 158.0.8127.36, light theme, measured viewport and PNG 1280×900, scale 1.
Four fixed active players, six saved spins (four repeated, two alternate), normal
local app/auth, animations and external avatars disabled. Source `fafed2acb`, dirty
only for investigation/test setup at capture. The existing capture task was
adapted temporarily for the reserved `visual-evidence-cpl24-statistics-v1` party;
no new capture framework or maintained fixture is required. Temporary capture
edits have been removed. Matched after capture: `48e71948-a45b-4af2-be9a-16ca0be0d7ea`;
expanded table: `8256767c-94fb-4555-912c-baf01022c6b6`, both complete under the same
browser/viewport conditions. Browser assertions verified baseline 2.0, six unique
pair rows, and above/below differences through the assembled query/view.
[Uploaded captures](https://github.com/robertfmurdock/Coupling/issues/3483#issuecomment-7109911825).
The initial after run `88854b35-384d-49c9-ba82-40e896599a94` was rejected: deleting
and recreating the reserved party retained its history, yielding twelve spins.
Fresh per-run party IDs restored the six-spin fixture; names, player IDs and
pairing pattern matched. Small random card tilts differ. Expanded rows extend
below the viewport and remain available through ordinary vertical scrolling.
No screen-reader calibration or narrow-width reflow claim is made.

## Current evidence and next action

The Statistics baseline regression failed meaningfully (147 component tests,
one failure for missing baseline text). An initial `--tests` invocation was rejected
by the aggregate JS task before tests ran; use the owning task without that option.
Baseline/table implementation and five shared calculation scenarios subsequently
passed `:client:components:jsTest :libraries:action:jvmTest`.
After formatting and test readability improvements, the focused tasks passed again.
Test review: the owning composition proves baseline delivery; component scenarios
exercise the real disclosure and assert individual table cells, including above,
below, equal, zero, unavailable and never-paired values. Shared tests establish
fractional short histories, five-cycle cap, empty/missing history and zero period.
Production review: no new UI dependencies or logging, one shared five-cycle
constant, party-scope history count via the existing query, nullable baseline for
unavailable inputs, and changes confined to the Statistics view. No contribution
or scheduling behavior changes. Full `./gradlew check --quiet` passed outside the sandbox, including the normal
application/browser and SDK suites. Uploaded attachment bytes were downloaded and
verified against all three local PNGs. First-slice implementation commit: `8f4974a24`.
No release, push or deployment is authorized.


## Cell-annotation slice · 2026-11-23

The owner was unsure about the original green-above/red-below proposal. Delivery
uses neutral numerical differences, keeps the existing fill palette, and adds an
inset black outline to known two-player cells within one pairing of the exact
baseline. The legend explains the ±1 marker. This replaces the contradictory
health-color proposal; contribution heatmaps remain unchanged.

Cells carry names, recent count, expected count, difference and last-paired spins
as native title descriptions and explicit accessible names. The semantic table
is the non-hover alternative; passive cells are not added to the Tab sequence.
The generic graphing cell owns only its value, description and outline flag;
Statistics owns the pairing interpretation. D3 now joins existing/new/removed
cells so data changes update descriptions, outlines and cell count together.

Red: `:client:components:jsTest --quiet` ran 150 tests with one meaningful failure,
missing the assembled cell's descriptive accessible name. After implementation,
154 component tests passed, including marker boundaries at expected ±1, missing
counts/baselines and a rerender that replaces descriptions, clears an old marker
and removes obsolete cells. Test review kept setup's prior rendering as a
prerequisite and asserted its captured observation in verification; table data
remains covered by the first slice. Production review confirmed the same color
interpolator and 90px cells, inset outlines, shared numerical formatting and no
new interaction framework or dependencies.

The production-built Chrome probe passed with baseline 2.0, four computed 2px
solid outlines and twelve off-diagonal title/accessible-name descriptions.
Final capture `4a816160-0f65-47d3-80fc-7853309d313d` is complete at measured
1280×900 viewport/PNG, light, scale 1, Chrome 158.0.8127.36. The previous matched
after capture is this slice's before. Temporary probe changes have been removed.
Final `./gradlew check --quiet` passed outside the sandbox. Both uploaded cell-slice
PNGs were downloaded and verified byte-for-byte against the local captures.
[Cell-slice evidence](https://github.com/robertfmurdock/Coupling/issues/3483#issuecomment-7110106596).
The implementation commit containing this archive is recorded in the visual issue's
closeout alongside first-slice commit `8f4974a24`.

## Completion review

The outcome serves actual saved pairing history on Statistics, with a short-history
baseline capped consistently at five full rotations. Unknown values remain unknown,
unique active-player pairs appear in the table, and descriptions and markers update
on rerender. Existing color fills, grid dimensions, Recent Pairing chart and
contribution views are preserved. The original contradictory health-color proposal
is replaced with descriptive counts plus the ±1 marker; no scheduling quality claim
is made. No maintained investigation code, extra fixture or new dependency remains.

Both implementation slices passed their focused checks and full repository check.
Matched local Chrome evidence proves assembled six-spin data and visible markers;
screen-reader calibration, native keyboard-only operation and narrow-width reflow
were not independently exercised. No production deployment or real-team observation
is claimed. The card is complete under this recorded scope and is archived.
No push, deployment or release was performed.
