# Add Contribution Velocity Trend Visualization

## Goal
Create a dual-axis line chart showing team throughput trends (contributions per time unit) alongside median cycle time to identify productivity patterns and correlate velocity with quality/speed.

## User Value
Tech leads can:
- Spot productivity trends (increasing/decreasing velocity)
- Correlate velocity with cycle time (are we rushing and cutting corners?)
- Identify seasonal patterns or team rhythm
- Validate impact of process changes

## Design Specification

**Visualization Type:** Dual-axis line chart with moving averages  
**Library:** Nivo `@nivo/line` with dual Y-axes

**Data Requirements:**
- Source: `ContributionReport.count` and `ContributionReport.medianCycleTime`
- X-axis: Time (bucketed by day/week based on window)
- Left Y-axis: Contribution count per period
- Right Y-axis: Median cycle time (Duration)

**Lines to Display:**
- Contributions per period (solid line, left axis)
- Median cycle time per period (solid line, right axis, different color)
- 4-week moving average for contributions (dashed, left axis)
- 4-week moving average for cycle time (dashed, right axis)

**Styling:**
- 600x600px container with white background
- Distinct colors for contribution vs cycle time metrics
- Dual Y-axis labels clearly marked
- Legend showing all four lines

**Interaction:**
- Hover: Show date, contribution count, cycle time, moving averages
- Highlight anomalies: Sudden drops/spikes (>50% change week-over-week)
- Optional annotations for external events (releases, incidents, holidays)

## Technical Implementation

### Files to Create
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/ContributionVelocityTrendGraph.kt`
2. `client/components/graphing/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/graphing/MovingAverage.kt` - Utility for moving average calculation

### Files to Modify
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/stats/PairFrequencyControls.kt`
   - Add `ContributionVelocityTrend` to `Visualization` enum

2. `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/contribution/ContributionVisualization.kt`
   - Add `when` branch for `ContributionVelocityTrend`

### Data Transformation Logic
```kotlin
// Time-bucket contributions (group by day/week/month based on window)
// For each bucket:
//   - Count contributions
//   - Calculate median cycle time from contributions in bucket
// Calculate moving averages (4-period window)
// Format for Nivo dual-axis line chart
```

### Moving Average Utility
```kotlin
fun <T> List<T>.movingAverage(
    windowSize: Int,
    selector: (T) -> Number
): List<Pair<T, Float>> {
    return windowed(windowSize, partialWindows = false) { window ->
        val avg = window.map { selector(it).toFloat() }.average().toFloat()
        window.last() to avg
    }
}
```

### Nivo Dual-Axis Configuration
Nivo's `@nivo/line` supports dual Y-axes via `axisRight` prop and series `yAxisId`:
```kotlin
// Configure series with different yAxisId
// Left axis: contributions count
// Right axis: cycle time duration (convert to hours/days for display)
```

## Constraints
- Use existing `ContributionReport` data (no GraphQL changes)
- Cycle time may be null for some contributions - handle gracefully
- Moving average calculation should handle edge cases (short time windows)

## Validation
- Generate fake data with known patterns:
  - Increasing velocity
  - Decreasing velocity with increasing cycle time (rushing)
  - Stable velocity
- Verify moving average smooths correctly (manual calculation check)
- Test dual-axis scaling (ensure both metrics are readable)
- Confirm hover shows all metrics correctly
- Run `./gradlew :client:check`

## Open Questions
- Should we allow toggling moving average window size (4-week vs 2-week)?
- Should anomaly detection be automatic or manual annotation?
