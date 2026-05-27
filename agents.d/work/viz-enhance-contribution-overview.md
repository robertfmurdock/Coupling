# Enhance Contribution Overview with Quick Stats

## Goal
Add stats panel above contribution list on `/:partyId/contributions` for immediate team health pulse check.

## Constraints
- No performance regression on page load
- Consistent styling with existing Coupling components (amber theme, TeamStatistics pattern)
- Gracefully handle missing data (null cycle time, zero contributions)
- **TDD required: test → fail → implement → pass → refactor per slice (see PLAYBOOK_CODE_STYLE.md)**

## Checklist
- [ ] Review card for template compliance
- [ ] Identify test files and patterns to follow
- [ ] Slice 1: Enhance GraphQL query to fetch report stats
  - [ ] Write test: verify query includes count, medianCycleTime, withCycleTimeCount, contributors
  - [ ] Add fields to `contributionOverviewPageQuery`
  - [ ] Run codegen: `./gradlew :client:generateApolloSources`
  - [ ] Verify test passes
- [ ] Slice 2: Create ContributionQuickStats component
  - [ ] Write test: component renders with stats data, handles null cycle time
  - [ ] Implement component (follow TeamStatistics.kt pattern)
  - [ ] Verify test passes
- [ ] Slice 3: Wire stats into ContributionOverviewPage and ContributionOverviewContent
  - [ ] Write test: stats extracted from query result and passed to content
  - [ ] Update ContributionOverviewPage to extract stats
  - [ ] Update ContributionOverviewContent to accept and render stats
  - [ ] Verify test passes
- [ ] Visual verification in browser (after all slices pass)
  - Navigate to `/:partyId/contributions`
  - Verify stats display correctly
  - Test with null cycle time, zero contributions
  - Check mobile responsive
- [ ] Final refactor pass
- [ ] Review against applicable playbooks
- [ ] Move to agents.d/work_completed/

## Implementation Notes

**Reverted (2026-05-27):**
- Previous implementation skipped TDD cycle - violated project constraints
- All code changes reverted
- Work card updated to enforce test-first approach per slice
- Context docs updated to make TDD non-negotiable

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
- Commands (run after each slice):
  - `./gradlew :client:components:test` (component tests)
  - `./gradlew :client:test` (integration tests)
  - `./gradlew :client:generateApolloSources` (after query change)
  - `./gradlew :client:compileKotlinJs`
  - `./gradlew :client:lintKotlin`
  - Visual test: dev server → `/:partyId/contributions` (final verification only)
- Results: [fill as you go per slice]
