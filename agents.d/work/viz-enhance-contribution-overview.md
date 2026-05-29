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
- [ ] Slice 1: Create ContributionQuickStats component that renders stats
  - [ ] Write test: component renders count, medianCycleTime, withCycleTimeCount, contributor count
  - [ ] Write test: component handles null medianCycleTime gracefully
  - [ ] Implement component (follow TeamStatistics.kt pattern)
  - [ ] Verify tests pass
  - [ ] Run: `./gradlew :client:components:test`
- [ ] Slice 2: Wire stats into ContributionOverviewContent
  - [ ] Write test: ContributionOverviewContent renders stats panel when stats provided
  - [ ] Add stats parameter to ContributionOverviewContent
  - [ ] Wire up ContributionQuickStats component
  - [ ] Verify tests pass
  - [ ] Run: `./gradlew :client:components:test`
- [ ] Slice 3: Extract stats from query and pass to content
  - [ ] Update GraphQL query to include: count, medianCycleTime, withCycleTimeCount, contributors
  - [ ] Run codegen: `./gradlew :client:generateApolloSources`
  - [ ] Update ContributionOverviewPage to extract stats from query result
  - [ ] Pass stats to ContributionOverviewContent
  - [ ] Verify compilation: `./gradlew :client:compileKotlinJs`
- [ ] Visual verification in browser
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

**Reverted (2026-05-29):**
- Query change without test - same mistake
- Treated GraphQL query change as "the feature" when it's just plumbing
- Real feature = component rendering stats, not query having fields
- Reorganized slices: component test → content test → wire query (setup, not TDD)
- Contributor type has only `playerId` and `email` (no name field - for Slice 3)

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
  - `./gradlew :client:components:test` (component tests - Slices 1 & 2)
  - `./gradlew :client:compileKotlinJs` (compilation - Slice 3)
  - `./gradlew :client:generateApolloSources` (codegen - Slice 3)
  - Visual test: dev server → `/:partyId/contributions` (final verification only)
- Results: [fill as you go per slice]
