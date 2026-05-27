# Contribution Visualizations Reference

## Purpose

Coupling's contribution visualization system helps tech leads and coaches understand team collaboration patterns, identify pairing gaps, monitor contribution health, and spot trends in team effectiveness.

## Architecture Overview

**Primary Libraries:**
- **Nivo Charts** (`@nivo/*`) - Primary charting library for most visualizations
- **Recharts** - Used for statistics page responsive line charts
- **D3** - Custom heatmap rendering with force-directed layouts

**Key Locations:**
- Visualization components: `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/`
- Visualization coordinator: `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/contribution/ContributionVisualization.kt`
- Visualization enum and controls: `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/stats/PairFrequencyControls.kt`
- Statistics page: `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/stats/PartyStatistics.kt`
- Graphing utilities: `client/components/graphing/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/graphing/`

## Data Model

### Contribution Entity
Location: `libraries/model/src/commonMain/kotlin/com/zegreatrob/coupling/model/Contribution.kt`

**Key Fields:**
- `participantEmails: Set<String>` - Who contributed
- `dateTime: Instant?` - When contribution occurred
- `integrationDateTime: Instant?` - When code was integrated/merged
- `cycleTime: Duration?` - Time from creation to integration
- `ease: Int?` - Story points or efficiency metric
- `story: String?` - Associated story/ticket
- `commitCount: Int?` - Number of commits
- `label: String?` - Custom tag for filtering

### ContributionReport
Aggregated metrics for a party or pair:
- `contributions: List<PartyRecord<Contribution>>` - Raw contribution list
- `count: Int` - Total contribution count
- `medianCycleTime: Duration?` - Median cycle time
- `withCycleTimeCount: Int?` - Count with cycle time data
- `contributors: List<Contributor>` - Unique contributors mapped to players

### PairingSet
Historical pair rotation data:
- `date: Instant` - When pairing was assigned
- `pairs: List<PinnedCouplingPair>` - Pairs assigned in this rotation
- `recentTimesPaired: Int` - Computed pairing frequency

### PlayerPair
Statistics for a specific pair:
- `players: List<Player>` - The two (or more) players
- `spinsSinceLastPaired: Int?` - Rotations since last pairing
- `pairAssignmentHistory: List<PairingSet>` - Full pairing history

## Current Visualizations (11 Types)

### Contribution Volume
1. **AllContributionsLineOverTime** - Total contributions over time (line graph)
2. **PairContributionsLineOverTime** - Per-pair contribution trends (multi-line graph)
3. **StoryContributionsOverTime** - Story-based contributions (line graph)
4. **StoryContributionsPercentOverTime** - Percentage contributions by story (line graph)

### Efficiency/Ease Metrics
5. **AllEaseLineOverTime** - Overall ease trends (line graph)
6. **PairEaseLineOverTime** - Ease trends per pair (multi-line graph)
7. **PairEaseHeatmap** - 2D heatmap of ease metrics per pair (Nivo heatmap)
8. **StoryEaseGraph** - Story-based ease visualization (Nivo line chart)

### Cycle Time
9. **MedianCycleTimeBarChart** - Median cycle time per pair (Nivo bar chart)
10. **CycleTimeBoxPlot** - Cycle time distribution (Nivo box plot)

### Pairing Frequency
11. **PairFrequencyHeatmap** - 2D heatmap showing pairing frequency (Nivo heatmap)

### Statistics Page (Separate Route)
- **PlayerHeatmap** - D3-based heatmap of player pairing frequency
- **Recent Pairing Line Chart** - Recharts line showing recent pairing history over time

## Interactive Features

**Time Windows** (enum: `ContributionWindow`):
- All, Year, HalfYear, Quarter, Month, Week

**Filtering:**
- Pair selector - Filter to specific pairs
- Label filter - Filter contributions by custom tags
- Fake data generator - Test with synthetic data patterns

**Controls:**
- Time window dropdown
- Visualization type dropdown
- Pair multi-select
- Label/tag filter

## Routes

- `/:partyId/contributions` - ContributionOverviewPage (5 most recent)
- `/:partyId/contributions/list` - ContributionListPage (detailed list)
- `/:partyId/contributions/visualization` - ContributionVisualizationPage (interactive charts)
- `/:partyId/statistics` - StatisticsPage (pairing statistics and heatmap)

## GraphQL Queries

**Contribution Queries:**
- `contributionVisualizationDataQuery` - Fetch pair contribution reports with window filtering
- `contributionListPageQuery` - Detailed contributions for a period
- `contributionOverviewPageQuery` - Recent contributions (limit 5)

**Statistics Queries:**
- `StatisticsPageQuery` - Pair statistics and pairing history
- `partyMedianSpinDurationQuery` - Median time between rotations
- `partySpinsUntilFullRotationQuery` - Spins needed for full rotation
- `partyPairsRecentTimesPairedQuery` - Recent pairing frequency

## Key Metrics Tracked

**Contribution Metrics:**
- Contribution count (raw and over time)
- Cycle time (median and distribution)
- Ease/story points (per contribution, aggregated)
- Commit count
- Contributor participation

**Pairing Metrics:**
- Pairing frequency (how often pairs work together)
- Recent times paired (rolling count)
- Spins since last paired (rotations without this pair)
- Median spin duration (average time between rotations)
- Spins until full rotation (team size - 1, adjusted for even teams)

## Component Patterns

### Adding a New Visualization

1. **Add enum value** to `Visualization` in `PairFrequencyControls.kt`
2. **Create component** in `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/`
   - Follow naming: `<VisualizationName>Graph.kt` or `<VisualizationName>Chart.kt`
   - Use Nivo components from `client/components/graphing/src/jsMain/kotlin/.../external/nivo/`
3. **Wire to coordinator** in `ContributionVisualization.kt` - Add `when` branch
4. **Pass data** as `List<Pair<CouplingPair, ContributionReport>>` or aggregated form
5. **Style** with 600x600px container, white background, circular border

### Component Props Pattern
```kotlin
external interface MyVisualizationProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: ContributionWindow
    var spinsUntilFullRotation: Int? // if pairing-related
}
```

### Data Transformation Pattern
Most visualizations need to transform pair contribution data:
```kotlin
private fun transformData(data: List<Pair<CouplingPair, ContributionReport>>): ChartData {
    // Extract contributions
    val allContributions = data.flatMap { it.second.contributions.elements }
    
    // Group/aggregate as needed
    // Convert to chart library format (Nivo, Recharts, D3)
    
    return chartData
}
```

## Testing Approach

**Fake Data Generation:**
- `FakeDataStyle.RandomPairs` - Random pairing patterns
- `FakeDataStyle.RandomPairsWithRandomSolos` - Mix of pairs and solo work
- `FakeDataStyle.StrongPairingTeam` - Consistent strong pairing

**Enable via UI:** Checkbox in PairFrequencyControls enables fake data for development/testing

## Historical Data Storage

- **Contributions:** Stored indefinitely in DynamoDB CONTRIBUTION table
- **Pair Rotations:** All PairingSet records in PAIR_ASSIGNMENTS table
- **Retention:** No automatic purging; complete history available
- **Soft Deletes:** Uses `isDeleted` flag rather than hard deletion

## Common Calculation Utilities

Location: `libraries/action/src/commonMain/kotlin/com/zegreatrob/coupling/action/stats/`

- **MedianSpinDuration** - Median time between consecutive pair assignments
- **SpinsUntilFullRotation** - `numPlayers.ifEvenSubtractOne()`
- **Heatmap Data** - Pairing frequency matrix with visual heat increments
- **PairReport** - Frequency and time since last pairing per pair

## Design Conventions

- **Container size:** 600x600px for most visualizations
- **Background:** White with circular border radius (150px)
- **Colors:** Nivo pastel1 palette for consistency
- **Tooltips:** Interactive with player names, dates, metrics
- **Legends:** Toggle visibility per series
- **Responsive:** Charts resize to container
