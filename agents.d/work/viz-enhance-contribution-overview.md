# Enhance Contribution Overview Page with Quick Stats Panel

## Goal
Add a quick stats panel above the contribution list on the Contribution Overview Page showing key metrics at a glance, giving coaches an immediate "pulse check" on team health.

## User Value
Tech leads can:
- Get immediate status without navigating to visualizations
- See trends at a glance (up/down indicators)
- Compare current period to previous period quickly
- Spot anomalies in recent activity

## Design Specification

**Location:** Above the 5 recent contributions list on `/:partyId/contributions`

**Panel Layout:** Horizontal card-style layout with 4-5 metrics

**Metrics to Display:**
1. **Total Contributions (This Week)**
   - Number with trend indicator (↑ +5 / ↓ -3 from last week)
   - Color: Green if up, red if down, gray if unchanged

2. **Total Contributions (This Month)**
   - Number with trend indicator vs last month
   - Same color coding

3. **Active Contributors (This Period)**
   - Count of unique contributors this week
   - Trend vs last week

4. **Median Cycle Time**
   - Formatted duration (e.g., "2.5 days")
   - Trend indicator (↓ faster is green, ↑ slower is red)
   - Include count: "across 12 contributions"

5. **Team Velocity** (Optional)
   - Contributions per active contributor
   - Indicates productivity per person

**Styling:**
- Card-style panel with subtle border/shadow
- Each metric in its own mini-card or section
- Icons for each metric type
- Responsive layout (stacks on mobile)

## Technical Implementation

### Files to Modify
1. `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/contribution/ContributionOverviewPage.kt`
   - Add stats panel component above contribution list

2. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/ContributionOverviewContent.kt`
   - Add QuickStatsPanel component render

### Files to Create
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/ContributionQuickStatsPanel.kt`
   - Main stats panel component

2. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/StatCard.kt`
   - Reusable stat card component

### GraphQL Query Enhancement
Current query: `contributionOverviewPageQuery` fetches 5 recent contributions

**Enhancement needed:**
```graphql
query ContributionOverviewPageQueryEnhanced($partyId: PartyId!, $limit: Int!) {
  party(id: $partyId) {
    id
    # Existing fields...
    
    # This week
    contributionsThisWeek: contributionReport(window: Week) {
      count
      medianCycleTime
      withCycleTimeCount
      contributors { playerDetails { id } }
    }
    
    # Last week (for comparison)
    contributionsLastWeek: contributionReport(window: Week, offset: 1) {
      count
      medianCycleTime
    }
    
    # This month
    contributionsThisMonth: contributionReport(window: Month) {
      count
    }
    
    # Last month (for comparison)
    contributionsLastMonth: contributionReport(window: Month, offset: 1) {
      count
    }
  }
}
```

**Note:** Check if GraphQL schema supports `offset` parameter for window queries. If not, may need to add or use custom date range.

### Component Structure
```kotlin
external interface ContributionQuickStatsPanelProps : Props {
    var thisWeek: ContributionReport
    var lastWeek: ContributionReport
    var thisMonth: ContributionReport
    var lastMonth: ContributionReport
}

@ReactFunc
val ContributionQuickStatsPanel by nfc<ContributionQuickStatsPanelProps> { props ->
    div {
        css {
            display = Display.flex
            gap = 16.px
            padding = 16.px
            backgroundColor = Color("#f9f9f9")
            borderRadius = 8.px
            marginBottom = 20.px
        }
        
        StatCard(
            label = "This Week",
            value = props.thisWeek.count.toString(),
            trend = calculateTrend(props.thisWeek.count, props.lastWeek.count),
            trendLabel = "vs last week"
        )
        
        StatCard(
            label = "This Month",
            value = props.thisMonth.count.toString(),
            trend = calculateTrend(props.thisMonth.count, props.lastMonth.count),
            trendLabel = "vs last month"
        )
        
        StatCard(
            label = "Active Contributors",
            value = props.thisWeek.contributors.size.toString(),
            subtitle = "this week"
        )
        
        props.thisWeek.medianCycleTime?.let { cycleTime ->
            StatCard(
                label = "Median Cycle Time",
                value = formatDuration(cycleTime),
                trend = calculateCycleTimeTrend(
                    cycleTime, 
                    props.lastWeek.medianCycleTime
                ),
                subtitle = "across ${props.thisWeek.withCycleTimeCount} contributions"
            )
        }
    }
}

fun calculateTrend(current: Int, previous: Int): Trend {
    val diff = current - previous
    return Trend(
        direction = when {
            diff > 0 -> TrendDirection.UP
            diff < 0 -> TrendDirection.DOWN
            else -> TrendDirection.NEUTRAL
        },
        value = abs(diff),
        percentage = if (previous > 0) (diff.toFloat() / previous * 100).roundToInt() else 0
    )
}
```

## Constraints
- GraphQL schema may not support offset parameter for windows - may need to fetch and filter client-side or add schema enhancement
- Should not significantly slow down page load (parallel queries preferred)
- Must be responsive and work on mobile
- Keep styling consistent with existing Coupling design

## Alternative Approach (If GraphQL offset not available)
Fetch contributions for wider window (e.g., Month) and aggregate client-side:
```kotlin
// Filter contributions into this week vs last week
val now = Clock.System.now()
val oneWeekAgo = now - 7.days
val twoWeeksAgo = now - 14.days

val thisWeekContributions = allContributions.filter { 
    it.dateTime in oneWeekAgo..now 
}
val lastWeekContributions = allContributions.filter { 
    it.dateTime in twoWeeksAgo..oneWeekAgo 
}
```

## Validation
- Verify trend calculations are correct (test with known data)
- Test with edge cases: zero contributions, equal contributions
- Ensure panel renders quickly (no performance regression)
- Test responsive layout on mobile
- Run `./gradlew :client:check`
- Visual test in browser with various data scenarios

## Open Questions
- Should we add more metrics (e.g., ease/story points)?
- Should stats panel be collapsible for users who want more list space?
- Should we support clicking a stat to navigate to full visualization?
