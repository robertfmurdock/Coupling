# CPL-27 — Show contribution participation by player over time

**Work card protocol: 2**

## Goal

Add “Contribution participation” to the existing contributions visualization
route, showing recorded participation over time in count and percentage modes.
Reuse the pair, label and time-window controls and preserve other visualizations.
The view supports comparison with the team's own expectations; counts do not
establish workload, productivity or engagement. Follow the
[collaboration metrics philosophy](../context/PERSONA.md#collaboration-metrics-and-team-agency).

Status: planned; no implementation yet.

## Product Contract

- Apply existing filters, then deduplicate records by contribution ID within the
  party. Each resolved participant counts once per contribution, including when
  multiple recorded emails belong to the same player. Shared contributions count
  for every participant, so total participations can exceed contribution count.
- Percentage is a player's count divided by all participations in that day,
  multiplied by 100. Label it “Share of recorded participations.” For example,
  one contribution with two participants gives each a count of 1 and share of 50%.
  Empty days have zero counts and no percentage value.
- Resolve players using existing email matching and stable player IDs. Keep
  unmatched emails as distinct recorded participants and disambiguate duplicate
  display names. Filter changes must not change a participant's identity.
- Group by `dateTime` in the browser's current time zone, using daily buckets and
  zero counts for intervening days. Disclose records omitted for missing dates
  and records without participants; do not substitute another timestamp.
- Use a stacked chart with stable series order/colors and no smoothing that
  implies fractional daily activity. Include explicit units, a short explanation
  of counting/date semantics, and a clear empty state.
- Provide the same labels and values in an accessible structured view using the
  same aggregates. Use labeled native controls for mode selection and an
  informational legend. Preserve desktop placement and keep the new view usable
  at narrow widths and zoom.

## Implementation Starting Points

Client-owned aggregation → chart and structured values. No API change is planned;
confirm the current query supplies the required records and player identities.

- `ContributionVisualization.kt` maps query results and selects the view. Its
  `allContributions()` flattens reports without deduplication; the mapper leaves
  `contributors` empty. Obtain player identities independently of filtered records.
- `PairFrequencyControls.kt` owns filters, selection and the chart host.
- `StoryContributionGraph.kt` already uses Recharts stacked areas;
  `ContributionsByDate.kt` supplies local-date grouping. Reuse these capabilities,
  keeping participation aggregation separate from chart-library objects. The
  story chart's allocation rules do not apply here.

These files are under the existing client contribution and component boundaries;
no new chart dependency or shared aggregation framework is needed.

## Delivery Slice

One contributor delivers the selectable view, both modes and accessible values
as one useful slice under [AGENTS.md](../../AGENTS.md).

1. Confirm query coverage, report overlap and window bounds against adjacent tests.
2. Establish failing aggregation/component scenarios for shared contributions,
   duplicate reports, aliases, unmatched participants, duplicate names, missing
   dates/participants, empty data and gaps. Assert exact counts and percentages.
   Cover filter changes and mode selection through the owning composition.
3. Wire filtered distinct records and player identities into daily aggregates,
   then render the chart and structured values. Keep any necessary host changes
   scoped to this view.

Start component assertions in `:client:components:jsTest`; use
`:client:jsBrowserTest` for geometry or chart behavior requiring a browser.
Follow [Testing](../context/TESTING.md) and consult Modern Web Guidance before UI
implementation. Full validation and closeout follow the
[work-card protocol](../context/WORK_CHECKLIST.md).

## Acceptance Evidence

- Fixture results establish the product contract, and rendered chart/structured
  values agree after filters or mode changes.
- A populated production-built browser view demonstrates route selection, both
  modes, readable labels and empty/coverage states. Verify reachability at a
  measured narrow viewport and 200% zoom with long and duplicate player names.
- Verify native keyboard operation and interpretation of structured values with
  assistive technology; record browser/OS, fixture conditions and any gaps.
- Retain before/after captures under the shared
  [visual evidence protocol](../context/WORK_CHECKLIST.md#record-visual-evidence),
  plus validation results and implementation SHA.
