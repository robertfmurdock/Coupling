# CPL-24 — Compare recent pairings with an even split

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


## Presentation iteration · 2026-11-23

After the first delivery, the owner described the added area as “text dumpy,”
with the visualization key and data mixed together as flat text on the gray
background, and requested more playful styling. One complete UI slice owns this
iteration; metrics, server/query contracts, heatmap fills/outlines and the
surrounding Statistics controls are unchanged.

The area now has two framed cream panels: a compact visual key with the color ramp
and marker samples, and a purple/gold pairing scorecard. The reference is shown
prominently once with saved-spin context. A native About this reference disclosure
holds the longer caveat and window explanation; Pair breakdown has a separate
mint summary and pair count. Both disclosures can stay open together. The table
has a purple header, alternating rows, spacing, signed-difference badges and a
shared reference caption instead of repeating the same reference in every row.
Full above/below wording remains available as each difference cell's accessible
name. A focusable horizontal scrolling boundary keeps the table usable when it
needs more width; tiny reference layouts can wrap. Existing native markers and
focus indicators are preserved, with solid panel borders and system-color
fallbacks rather than relying on shadows or the decorative gradient alone.

Before tests, the declared contract separated the key from the scorecard and
made the explanation available on demand. The new component scenario failed
meaningfully for the absent disclosure (155 tests, one failure). After the
presentation change, all 155 component tests passed. Existing populated-row,
unknown-count and assembled zero-history assertions were updated to the new
presentation without losing their numerical or accessible-description coverage.
CSS-only variations were checked in rendered Chrome, not with CSS-string tests.

Test review: the new scenario exercises opening the explanation and checks the
other disclosure remains closed; table scenarios still exercise opening real
pair data, and verify the reference once in the caption plus every row's actual
count, signed difference, full accessible description and last-paired state.
Production review: native details/summary, scoped CSS, typed colors/gradient, no
new JS interaction state, dependencies or maintained capture tooling. Numerical
calculation and existing cell descriptions are unchanged.

[Presentation evidence](https://github.com/robertfmurdock/Coupling/issues/3483#issuecomment-7113400763).
The prior final capture `4a816160-0f65-47d3-80fc-7853309d313d` is the applicable
before state for parent `931c9b541`. Collapsed after:
`be248818-b27d-43a2-8820-bf93b3c5a3fc`, measured viewport/PNG 1280×900.
Expanded final source: `f977cf65-f44d-4f74-86c6-de640104b4aa`, 1280×1350 to show
all six rows and both open disclosures. All complete, Chrome 158.0.8127.36, light,
scale 1, en-US/UTC, same synthetic four-player/six-spin pattern. Fresh reserved
party IDs avoid retained history; small random card tilts differ. The collapsed
capture remains applicable after final forced-colors and tiny-panel wrapping
additions, which do not alter that four-player/light view. Earlier intermediate
captures were used only for iteration. Uploaded PNGs were downloaded and matched
byte-for-byte. Temporary fixture and keyboard-binding changes were removed.

Native Chrome keyboard evidence now closes that earlier local gap: Enter opens
and closes the explanation, Tab reaches Pair breakdown with visible focus, Enter
opens the breakdown, and Tab reaches the scrollable table. The browser probe also
verified the reference and six unique rows with complete accessible differences.
New text color pairs, including hover, calculate to at least 5.85:1. Calibrated
screen readers, forced-colors emulation and full-page narrow reflow remain
unverified; no claim is made for those conditions.

`./gradlew check --quiet` passed outside the sandbox for this complete slice.
The presentation implementation SHA is recorded in the visual issue closeout.
No push, release or deployment was performed.


## Neutral comparisons and domain philosophy · 2026-11-23

Owner clarification supersedes this card's earlier expected-count, scorecard and
±1-band choices. The app should anchor a lead's and team's decisions in what
happened, support questions when patterns are surprising, and preserve the choice
not to act. Accurate reporting must not feel penalized. The normal pairing
algorithm remains a comprehensible, predictable starting suggestion that people
can override without justifying themselves to the system; its coordination benefit
and gentle nudge are intentional. No pairing algorithm or override behavior changed.

The authoritative principle is now in
[Collaboration Metrics and Team Agency](../context/PERSONA.md#collaboration-metrics-and-team-agency),
with task-based discovery in AGENTS.md, the context index and visualization guide.
That guidance incorporates the owner's clarification about leadership context,
comprehensibility, team agency, truthful inputs and understandable defaults. It
allows useful interpretation and suggestions while rejecting app-imposed verdicts
and required compliance. Historical risk/health/ideal proposals must be reviewed
against that current guidance before implementation.

UI delivery: Pairing distribution, Even split per pair, and more/fewer than an even
split in cell/table accessible descriptions. The explanation explicitly says this
is not a goal or prediction and pairing decisions belong to the team. Remove the
arbitrary ±1 proximity outlines and legend item, rather than merely relabeling a
preferred band. The unused renderer outline flag/styles and obsolete marker-only
test were removed; count, unavailable-data, accessible-description and D3 rerender
coverage remain. Actual counts, comparison arithmetic, five-cycle window and fill
palette are unchanged. The normal algorithm's deliberate nudge is separate from
this descriptive mathematical comparison.

Red: 156 component tests with two intended failures, for the missing new comparison
disclosure and missing neutral accessible cell description. After implementation,
155 component tests pass (one obsolete marker-classification scenario removed,
one neutral comparison scenario added). A remaining assertion about the old inline
outline property was removed as part of intentionally deleting that capability;
real-browser evidence verifies the user-visible absence of all proximity outlines.
Test review retains the numerical/table contract and full comparison wording, plus
the disclosure's explanation of team ownership. Production review confirms no
algorithm, scheduling, server or query changes and no new UI interaction framework.
No artificial tests were added for the documentation changes.

[Neutral comparison evidence](https://github.com/robertfmurdock/Coupling/issues/3483#issuecomment-7114764473).
Before: `be248818-b27d-43a2-8820-bf93b3c5a3fc`, 1280×900, applicable to parent
`85608c910`. Collapsed after: `5ee8dce8-8fb6-487a-93cb-d96bf2b4da48`, 1280×900.
Final expanded explanation/table: `5cefa9f6-cf04-4ae9-9e78-6165160a2725`, 1280×1350.
All complete, actual viewport and PNG measured, Chrome 158.0.8127.36, light, scale
1, en-US/UTC, same synthetic four-player/six-spin pattern. Fresh party IDs avoid
retained history; small random card tilts differ. The collapsed capture remains
applicable after the final hidden sentence was changed to “Pairing decisions belong
to your team”; the expanded capture uses final text. Browser assertions confirmed
zero outlined cells, unchanged numerical comparison, twelve neutral cell accessible
names and six unique rows with complete more/fewer descriptions. Temporary capture
edits were removed. Earlier local native keyboard evidence still applies to these
unchanged native disclosures; calibrated screen-reader, forced-colors and narrow
page reflow coverage remain unverified.

Full `./gradlew check --quiet` passed outside the sandbox. Local guidance links,
including the philosophy anchor, and `git diff --check` passed. The implementation
commit and attachment verification are recorded in the visual issue closeout.
No push, release or deployment was performed.
