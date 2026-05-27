# Add Player Contribution Balance Visualization

## Goal
Create a stacked area chart showing contribution distribution across team members over time to help coaches identify workload imbalance, disengagement, or onboarding progress.

## User Value
Tech leads can:
- Quickly spot players who are consistently under-contributing (may need support)
- Identify players who are overloaded (burnout risk)
- Track onboarding progress for new team members
- See team size changes and their impact on contribution patterns

## Design Specification

**Visualization Type:** Stacked area chart or stream graph  
**Library:** Nivo `@nivo/area` or `@nivo/stream`

**Data Requirements:**
- Source: `Contribution.participantEmails` grouped by player over selected time window
- X-axis: Time (bucketed by day/week based on window size)
- Y-axis: Contribution count or percentage
- Series: One per player, colored consistently with player badge color

**Interaction:**
- Hover: Show player name, contribution count, date
- Toggle: Switch between absolute counts and percentage view
- Filter: Respect existing pair/label filters

**Styling:**
- 600x600px container with white background
- Use Nivo pastel1 color palette
- Show legend with player names

## Technical Implementation

### Files to Create
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/PlayerContributionBalanceGraph.kt`

### Files to Modify
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/stats/PairFrequencyControls.kt`
   - Add `PlayerContributionBalance` to `Visualization` enum

2. `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/contribution/ContributionVisualization.kt`
   - Add `when` branch for `PlayerContributionBalance`

### Data Transformation Logic
```kotlin
// Group contributions by player and time bucket
// Convert participantEmails to Player objects
// Aggregate counts per player per time period
// Transform to Nivo area/stream data format
```

## Constraints
- Use existing `ContributionReport` data structure (no GraphQL changes)
- Follow existing component pattern from other graph components
- Maintain consistency with time window filtering

## Validation
- Generate fake data with unbalanced patterns
- Verify visual output shows clear imbalance
- Test hover interactions
- Confirm toggle between absolute/percentage works
- Run `./gradlew :client:check`
