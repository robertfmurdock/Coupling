# Add Player Isolation Risk Matrix Visualization

## Goal
Create a scatter plot or bubble chart identifying players at risk of knowledge silos based on their pairing patterns. Helps coaches prioritize who needs pairing attention.

## User Value
Tech leads can:
- Immediately see which players need pairing attention
- Prioritize who to pair next based on isolation risk
- Balance technical specialists who legitimately pair less
- Track impact of deliberate rotation interventions

## Design Specification

**Visualization Type:** Scatter plot with bubbles  
**Library:** Nivo `@nivo/scatterplot`

**Axes:**
- X-axis: Number of unique pair partners (in selected window)
- Y-axis: Spins since last rotation (time since pairing with someone new)
- Bubble size: Total contributions in period (larger = more active)
- Color: Risk score (green = healthy, yellow = watch, red = isolated)

**Risk Score Calculation:**
```kotlin
Risk Score = (1 / unique_partners) * spins_since_last_new_pair * (1 / total_contributions_ratio)
// Higher score = higher isolation risk
```

**Color Thresholds:**
- Green (healthy): Risk < 0.3, or >5 unique partners
- Yellow (watch): Risk 0.3-0.7
- Red (isolated): Risk > 0.7, or <2 unique partners

**Data Requirements:**
- Source: `PlayerPair.spinsSinceLastPaired` + `Contribution.participantEmails`
- Aggregate per player:
  - Count unique pair partners in window
  - Max spins since last new partner
  - Total contributions count

**Interaction:**
- Hover: Show player name, stats breakdown, risk score
- Click: Navigate to player's pairing history detail
- Optional: Add quadrant lines (e.g., <3 unique partners = risk zone)

**Styling:**
- 600x600px container with white background
- Color-coded bubbles with player initials/avatars
- Legend explaining color coding

## Technical Implementation

### Files to Create
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/PlayerIsolationRiskMatrix.kt`
2. `libraries/action/src/commonMain/kotlin/com/zegreatrob/coupling/action/stats/IsolationRisk.kt` - Risk calculation logic

### Files to Modify
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/stats/PairFrequencyControls.kt`
   - Add `PlayerIsolationRisk` to `Visualization` enum

2. `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/contribution/ContributionVisualization.kt`
   - Add `when` branch for `PlayerIsolationRisk`
   - May need to query player-level aggregated data

### Data Aggregation
```kotlin
data class PlayerIsolationMetrics(
    val player: Player,
    val uniquePartners: Int,
    val spinsSinceLastNewPartner: Int,
    val totalContributions: Int,
    val riskScore: Float
)

fun calculatePlayerIsolation(
    players: List<Player>,
    contributions: List<Contribution>,
    pairingHistory: List<PairingSet>,
    window: Duration
): List<PlayerIsolationMetrics> {
    // For each player:
    // - Count unique partners from pairingHistory in window
    // - Find most recent pairing with a new partner, calculate spins since
    // - Count contributions by participantEmails in window
    // - Calculate risk score
}
```

### Risk Score Formula
```kotlin
fun calculateRiskScore(
    uniquePartners: Int,
    spinsSinceLastNewPartner: Int,
    contributionsRatio: Float // player contributions / avg team contributions
): Float {
    val diversityFactor = if (uniquePartners == 0) 1.0f else 1.0f / uniquePartners
    val recencyFactor = spinsSinceLastNewPartner.toFloat()
    val activityFactor = if (contributionsRatio == 0f) 1.0f else 1.0f / contributionsRatio
    
    return diversityFactor * recencyFactor * activityFactor
}
```

## Constraints
- Requires both contribution and pairing history data
- May need to extend GraphQL query to include player-level aggregations
- Should handle edge cases: new players, players on PTO, specialists

## Validation
- Generate fake data with:
  - Balanced player (green)
  - Isolated player (red)
  - New player (edge case)
- Verify risk score calculation matches expectations
- Test bubble sizing relative to contribution volume
- Confirm color coding thresholds work visually
- Run `./gradlew :client:check` and `./gradlew :libraries:action:check`

## Open Questions
- Should PTO/vacation players be filtered out or marked differently?
- Should specialists (e.g., only pairs with 1-2 people on specific work) have adjusted thresholds?
