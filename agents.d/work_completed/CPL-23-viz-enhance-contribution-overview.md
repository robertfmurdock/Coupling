# CPL-23 — Enhance Contribution Overview with Quick Stats

**Work card protocol: 2**

## Goal and contract
Show a centered summary above the contribution overview list, including its empty
state. All metrics describe the same latest five contributions already requested
by the page: contribution count, records with cycle time, median cycle time, and
distinct participant email count. Missing median reads “Unavailable”; empty counts
read zero. These are sample statistics, not weekly, monthly, or all-time totals.
Keep the existing request limit, route, controls, and playful visual character.
User requested a nicer centered presentation and identified the contribution list
quantile section as a reference. Use its centered heading and evenly spaced metrics
rather than the inherited left-aligned bordered box; retain the page’s amber backdrop.

## History and inherited work
The May 27 and May 29 attempts were reverted for missing behavior-first tests,
including an attempt to treat query fields as the feature. No technical blocker
was recorded. The original trend comparisons were removed from scope on May 26.
Commit `159feab55` adds panel, query fields, page wiring, and E2E assertions.
Local `/private/tmp/cpl23-red.log` and `cpl23-green.log` show a missing-summary
failure and a successful focused E2E run. Before/after desktop/mobile captures
exist there, but the prior commit has no recorded full-suite gate.
The report's contributors field is currently empty server-side; count distinct
participant emails from the bounded returned contributions instead. Page-owned
composition intentionally replaces the obsolete content-prop checklist.

## Safe completion slice
- Owner: page selects/maps report data; panel renders semantic label/value pairs.
- Component scenarios in `:client:components:jsTest`: populated values, null
  median with contributions, and empty report. Validate sensitivity with a
  reversible rendering fault because implementation is inherited.
- E2E: seed more than five records with an old outlier and repeated participants;
  verify only the latest five determine every metric. Removing the query limit
  or counting participants without deduplication must fail these assertions.
- Keep query and rendering connected; remove screenshot side effects from normal
  E2E tests. Make sample scope visible. No separate plumbing-only commit.
- Review test clarity and production ownership separately; focused checks then
  full `./gradlew check --no-daemon`, followed by one safe completion commit.

## Acceptance evidence
Four metrics render correctly for populated, missing-cycle-time, and empty data.
An older sixth record cannot affect the displayed five-record summary. Panel
content remains readable at desktop and narrow/enlarged layouts. Preserve existing
controls. Compare inherited page overflow against before captures rather than
claiming the whole page is responsive. Keep the bounded existing network request;
any timing claims require matched repeated measurements.

## Visual evidence
https://github.com/robertfmurdock/Coupling/issues/3473

Inherited before/after screenshots uploaded with `gh issue comment --attach`.
The requested 390px mobile window actually produced a 485px image; corrected
metadata is recorded in the issue. Completion before/after captures are attached
in https://github.com/robertfmurdock/Coupling/issues/3473#issuecomment-7043393486 .
Actual sizes match: 1265×700 desktop and 485×629 narrow; theme and metric values
match. Stub IDs and unrelated contribution details vary. The summary wraps into
two complete rows on the narrow capture. Existing header/card overflow remains.
The viewport-bound wrapper fills its parent up to 75dvw; its inner section caps
reading width without expanding the surrounding page. Parent-relative maximum
width protects narrow containers; dynamic viewport sizing protects the summary
when unrelated page content overflows. Desktop placement of existing controls is
preserved. Capture bindings were temporary and are absent from committed tests.

## Validation and status
Complete, 2026-11-20. Completion commit is recorded on the visual evidence issue
and in this card’s Git history. Not pushed or released.
- Component fault check: all three scenarios failed on wrong count, as intended.
- Scope explanation scenario failed on missing explanatory text.
- Browser layout regression failed on viewport-based panel width in a 320px
  test-owned container at 32px text size.
- E2E fault check: raising query limit to six failed the latest-five scenario on
  the expected missing five-record heading. Restored five-record limit.
- Component and browser suites passed after first layout correction; repeat
  verification covers the subsequent user-requested presentation adjustment.
- Overflow protection: relaxing the wrapper bound to 200dvw fails
  `contributionSummaryStaysVisibleWhenSurroundingPageOverflows` on offscreen
  summary geometry; restored 75dvw.
- Final focused E2E: all three contribution overview scenarios passed.
- Test review: component variations stay in Node/jsdom; layout assertions use
  real Chrome and named test-owned containers; E2E exercises assembled query,
  mapping, and route with a deterministic old outlier and repeated participants.
- Production review: no new request, dependency, server calculation, or schema
  change; bounded existing query and page mapping remain intact. Presentation
  keeps native headings and definition lists. No speculative shared abstraction.
- Performance evidence is structural (same bounded request and existing server
  aggregation); authenticated route timing was not measured, and no timing or
  field Web Vitals improvement is claimed.
- Full gate passed: `fish -lc './gradlew :client:components:formatKotlin
  :client:formatKotlin :e2e:formatKotlin check --no-daemon'`, outside the sandbox
  with no E2E filter. 696 actionable tasks: 84 executed, 7 from cache, 605
  up-to-date. Log: `/private/tmp/cpl23-final-check.log`.
- Final reports: all three quick-stats component scenarios pass; all nine
  PageLayout scenarios pass, including both new geometry regressions. The full
  unfiltered E2E run passes (61 passed, zero skipped/failed), including all three
  contribution overview scenarios.
- Test clarity and production design reviewed separately; no outstanding findings.
  Visual evidence reviewed at desktop and narrow sizes; issue closure follows
  the completion commit. No inbound repository links required updating.
- First overlapping red runs corrupted reports; discarded those results and
  repeated the fault check sequentially for valid assertion evidence.
