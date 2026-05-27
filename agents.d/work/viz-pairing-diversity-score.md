# Add Pairing Diversity Score Over Time Visualization

## Goal
Create a line graph tracking how well the team maintains diverse pairing over time compared to ideal rotation patterns. Helps coaches see if the team is maintaining healthy rotation or drifting toward pairing silos.

## User Value
Tech leads can:
- See if team is maintaining healthy rotation patterns
- Identify periods of stagnant pairing (same pairs repeating)
- Track improvement after coaching interventions
- Get early warning when team is drifting toward silos

## Design Specification

**Visualization Type:** Multi-line graph with threshold indicators  
**Library:** Nivo `@nivo/line`

**Metric Calculation:**
```kotlin
Diversity Score = (Unique pairs this period / Total possible pairs) * 100
Ideal Score = (100 / spinsUntilFullRotation) * 100  // per rotation cycle
```

**Data Requirements:**
- Source: `PairingSet.pairs` aggregated over rolling windows
- X-axis: Time (bucketed appropriately for selected window)
- Y-axis: Diversity score percentage (0-100%)

**Lines to Display:**
- Actual diversity score (solid line, main metric)
- Ideal diversity threshold (dashed reference line)
- 3-rotation moving average (smoothed trend line)

**Color Coding:**
- Green: Above ideal threshold
- Yellow: Near ideal (±10%)
- Red: Below ideal threshold

**Interaction:**
- Hover: Show diversity score, date, which pairs were active
- Click point: Option to drill down to that period's pairing details

**Styling:**
- 600x600px container with white background
- Legend showing all three lines
- Y-axis from 0-100%

## Technical Implementation

### Files to Create
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/PairingDiversityScoreGraph.kt`
2. Potentially `libraries/action/src/commonMain/kotlin/com/zegreatrob/coupling/action/stats/DiversityScore.kt` - Calculation logic

### Files to Modify
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/stats/PairFrequencyControls.kt`
   - Add `PairingDiversityScore` to `Visualization` enum

2. `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/contribution/ContributionVisualization.kt`
   - Add `when` branch for `PairingDiversityScore`
   - May need to pass additional `pairingHistory` data

### Data Requirements
Current data from `ContributionReport` may not include pairing history. Options:
1. Use existing `PlayerPair.pairAssignmentHistory` if available in visualization context
2. Add pairing history to query if not already loaded
3. Consider adding computed field to GraphQL schema for server-side calculation

### Calculation Logic
```kotlin
fun calculateDiversityScore(
    pairingSets: List<PairingSet>,
    windowStart: Instant,
    windowEnd: Instant,
    totalPlayers: Int
): Float {
    val pairsInWindow = pairingSets.filter { it.date in windowStart..windowEnd }
    val uniquePairs = pairsInWindow.flatMap { it.pairs }.toSet()
    val totalPossiblePairs = (totalPlayers * (totalPlayers - 1)) / 2
    return (uniquePairs.size.toFloat() / totalPossiblePairs) * 100
}

fun calculateIdealScore(spinsUntilFullRotation: Int): Float {
    return (100f / spinsUntilFullRotation)
}
```

## Constraints
- May require loading pairing history data in visualization query
- Must respect existing time window filtering
- Keep calculation logic in shared `libraries/action` for reuse

## Validation
- Generate fake data with known diversity patterns (high, low, variable)
- Verify diversity score calculation matches manual calculation
- Test that ideal threshold line appears correctly
- Verify moving average smooths appropriately
- Run `./gradlew :client:check` and `./gradlew :libraries:action:check`
