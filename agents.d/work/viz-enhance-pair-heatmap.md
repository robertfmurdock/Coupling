# Enhance Pair Frequency Heatmap with Expected Baseline

## Goal
Improve the existing Pair Frequency Heatmap by adding annotations showing expected pairing frequency baselines, making it immediately obvious which pairs are under-rotating vs over-rotating.

## User Value
Coaches can:
- Immediately see which pairs are below expected rotation frequency
- Identify over-pairing (same pair too often)
- Understand variance from ideal rotation pattern at a glance
- Prioritize pairing adjustments based on deviation from baseline

## Design Specification

**Current Behavior:**
- Shows pairing frequency as color intensity (darker = more frequent)
- Tooltip shows raw count of pairings

**Enhancements:**
1. **Expected Frequency Baseline**
   - Calculate expected pairing frequency: `Total rotations / spinsUntilFullRotation`
   - Add visual indicator (subtle background pattern or border) for expected frequency

2. **Color Divergence**
   - Green tint: Above expected frequency (good rotation)
   - Red tint: Below expected frequency (needs attention)
   - Neutral: At expected frequency (±1 pairing)

3. **Enhanced Tooltip**
   - Actual count: "Paired 8 times"
   - Expected count: "Expected ~6 times"
   - Variance: "+2 above expected" or "-3 below expected"
   - Last paired date: "Last paired 3 spins ago"

4. **Legend Update**
   - Add legend explaining color divergence
   - Show expected frequency threshold

## Technical Implementation

### Files to Modify
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/PairContributionsHeatMap.kt`
   - Add expected frequency calculation
   - Modify color scale to show divergence
   - Enhance tooltip content

2. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/CouplingHeatmapTooltip.kt`
   - Add expected frequency fields
   - Show variance calculation

3. Potentially `libraries/action/src/commonMain/kotlin/com/zegreatrob/coupling/action/stats/PairReport.kt`
   - Add utility for expected frequency calculation if not already present

### Expected Frequency Calculation
```kotlin
fun calculateExpectedPairingFrequency(
    totalRotations: Int,
    spinsUntilFullRotation: Int,
    numPlayers: Int
): Float {
    // For a pair, expected frequency is:
    // Total rotations / spins needed for full rotation
    return totalRotations.toFloat() / spinsUntilFullRotation
}

fun calculatePairingVariance(
    actualCount: Int,
    expectedFrequency: Float
): Int {
    return actualCount - expectedFrequency.roundToInt()
}
```

### Color Divergence Implementation
```kotlin
// Modify Nivo heatmap color scheme
// Base color intensity from frequency (existing)
// Overlay divergence: green tint if above expected, red if below

fun getHeatmapCellColor(
    actualCount: Int,
    expectedCount: Float
): String {
    val variance = actualCount - expectedCount
    return when {
        variance > 1 -> "#e8f5e9" to "#2e7d32" // green scale
        variance < -1 -> "#ffebee" to "#c62828" // red scale
        else -> "#f5f5f5" to "#424242" // neutral gray scale
    }.let { (light, dark) ->
        // Interpolate based on actualCount intensity
        interpolateColor(light, dark, actualCount)
    }
}
```

### Tooltip Enhancement
```kotlin
external interface EnhancedHeatmapTooltipProps : Props {
    var pair: CouplingPair
    var actualCount: Int
    var expectedCount: Float
    var variance: Int
    var lastPairedDate: Instant?
    var spinsSinceLastPaired: Int?
}

@ReactFunc
val EnhancedHeatmapTooltip by nfc<EnhancedHeatmapTooltipProps> { props ->
    div {
        +"${props.pair.playerNames} paired ${props.actualCount} times"
        div {
            +"Expected: ~${props.expectedCount.roundToInt()} times"
        }
        div {
            css { color = if (props.variance > 0) Color("green") else Color("red") }
            +when {
                props.variance > 0 -> "+${props.variance} above expected"
                props.variance < 0 -> "${props.variance} below expected"
                else -> "At expected frequency"
            }
        }
        props.lastPairedDate?.let { date ->
            div { +"Last paired: ${date.formatRelative()}" }
        }
        props.spinsSinceLastPaired?.let { spins ->
            div { +"$spins spins ago" }
        }
    }
}
```

## Constraints
- Must not break existing heatmap functionality
- Color divergence should be subtle, not overwhelming the frequency data
- Expected frequency calculation should be clearly documented
- Backward compatible with existing fake data generation

## Validation
- Generate fake data with:
  - StrongPairingTeam (should show mostly green/neutral)
  - RandomPairs (should show more variance)
- Verify expected frequency calculation matches manual calculation
- Test tooltip shows all new fields correctly
- Confirm color divergence is visible but not distracting
- Ensure legend clearly explains color scheme
- Run `./gradlew :client:check`
- Test with real team data in browser

## Dependencies
Depends on existing:
- `PairContributionsHeatMap` component
- `CouplingHeatmapTooltip` component
- `spinsUntilFullRotation` calculation
