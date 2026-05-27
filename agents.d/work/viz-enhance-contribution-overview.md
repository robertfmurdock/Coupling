# Enhance Contribution Overview with Quick Stats

## Goal
Add stats panel above contribution list on `/:partyId/contributions` for immediate team health pulse check.

## Constraints
- No performance regression on page load
- Consistent styling with existing Coupling components (amber theme, TeamStatistics pattern)
- Gracefully handle missing data (null cycle time, zero contributions)

## Checklist
- [ ] Review card for template compliance
- [ ] Enhance GraphQL query to fetch report stats
  - Add `count`, `medianCycleTime`, `withCycleTimeCount`, `contributors` to `contributionOverviewPageQuery`
  - Verify schema supports needed fields
  - Run codegen: `./gradlew :client:generateGraphQL`
- [ ] Create ContributionQuickStatsPanel component
  - Follow TeamStatistics.kt pattern (white card, border, padding)
  - Show: contributions count, active contributors, median cycle time
  - Format duration with helper (check for existing utility)
- [ ] Update ContributionOverviewPage to extract and pass stats
  - Extract `count`, `medianCycleTime`, `withCycleTimeCount`, `contributors.size` from report
  - Pass to ContributionOverviewContent as props
- [ ] Insert stats panel in ContributionOverviewContent
  - Position above "Most Recent 5 Contributions" heading
  - Wire up props
- [ ] Visual test in browser
  - Navigate to `/:partyId/contributions`
  - Verify stats display correctly
  - Test with null cycle time, zero contributions
  - Check mobile responsive
- [ ] Final refactor pass
- [ ] Review against applicable playbooks
- [ ] Move to agents.d/work_completed/

## Implementation Notes

**Key Files:**
- Query: `client/src/commonMain/graphql/queries.graphql` (line 197-206)
- Page: `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/contribution/ContributionOverviewPage.kt`
- Content: `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/ContributionOverviewContent.kt`
- New component: `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/ContributionQuickStatsPanel.kt`

**Styling References:**
- `TeamStatistics.kt` - card pattern
- `StatsHeader.kt` - bold header
- `StatLabel.kt` - label + value pairs

## Validation
- Commands:
  - `./gradlew :client:generateGraphQL` (after query change)
  - `./gradlew :client:compileKotlinJs`
  - `./gradlew :client:check`
  - Visual test: dev server → `/:partyId/contributions`
- Results: [fill before completion]
