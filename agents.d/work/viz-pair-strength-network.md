# Add Pair Strength Network Graph Visualization

## Goal
Create a force-directed network graph visualizing the collaboration network, showing strong vs weak pairing connections to help coaches see the overall team connectivity at a glance.

## User Value
Tech leads can:
- Get visual "map" of team collaboration patterns
- Identify cliques or subgroups that don't mix
- See overall connectivity health at a glance
- Find "bridge" players who connect different groups

## Design Specification

**Visualization Type:** Force-directed network graph  
**Library:** D3 force simulation or Cytoscape.js/React-Force-Graph

**Network Elements:**
- **Nodes:** Players (sized by total contributions in window)
- **Edges:** Pairing relationships
  - Thickness: Proportional to pairing frequency
  - Color: Gradient from gray (old) to bright (recent)
  - Style: Solid for strong connections (>3 pairings), dashed for weak (<3)

**Layout:**
- Force-directed to naturally show clusters
- Players who pair often are pulled together
- Players who rarely pair are pushed apart

**Data Requirements:**
- Source: `PlayerPair` with `pairAssignmentHistory` and `spinsSinceLastPaired`
- Node data: Player info + contribution count
- Edge data: Pairing frequency + most recent pairing date

**Interaction:**
- Drag nodes: Reposition for better view
- Hover node: Highlight all connected edges and partners
- Hover edge: Show pairing details (frequency, last paired date)
- Click node: Highlight this player's subgraph
- Toggle views:
  - Frequency-weighted (default)
  - Recency-weighted (recent pairings more prominent)

**Styling:**
- 600x600px container with white background
- Node colors match player badge colors
- Grayscale to color gradient for edge recency

## Technical Implementation

### Files to Create
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/contribution/PairStrengthNetworkGraph.kt`
2. `client/components/graphing/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/graphing/ForceGraph.kt` - D3 force simulation wrapper (if using D3)

### Files to Modify
1. `client/components/src/jsMain/kotlin/com/zegreatrob/coupling/client/components/stats/PairFrequencyControls.kt`
   - Add `PairStrengthNetwork` to `Visualization` enum

2. `client/src/jsMain/kotlin/com/zegreatrob/coupling/client/contribution/ContributionVisualization.kt`
   - Add `when` branch for `PairStrengthNetwork`
   - May need to query full player and pairing data

### Library Choice Decision
**Option 1: D3 Force Simulation**
- Pros: Already used in codebase (PlayerHeatmap), full control
- Cons: More complex implementation, need to manage SVG rendering

**Option 2: React-Force-Graph**
- Pros: React-friendly wrapper, easier implementation
- Cons: Additional dependency, less customization

**Option 3: Cytoscape.js**
- Pros: Powerful graph library, good for network analysis
- Cons: Heavier dependency, different API style

**Recommendation:** Start with D3 force simulation for consistency with existing codebase.

### Data Transformation
```kotlin
data class NetworkNode(
    val player: Player,
    val contributionCount: Int,
    val x: Double? = null,
    val y: Double? = null
)

data class NetworkEdge(
    val source: Player,
    val target: Player,
    val frequency: Int,
    val lastPairedDate: Instant?,
    val daysSinceLastPaired: Int?
)

fun buildNetworkGraph(
    players: List<Player>,
    playerPairs: List<PlayerPair>,
    contributions: List<Contribution>,
    window: Duration
): Pair<List<NetworkNode>, List<NetworkEdge>> {
    // Build nodes with contribution counts
    // Build edges with pairing frequency and recency
}
```

### Force Simulation Parameters
```kotlin
// D3 force simulation configuration
forceSimulation()
    .force("link", linkForce().distance { edge -> 
        // Shorter distance for more frequent pairs
        100 / (edge.frequency.coerceAtLeast(1))
    })
    .force("charge", manyBody().strength(-200))
    .force("center", center(width / 2, height / 2))
    .force("collision", collision().radius { node -> 
        // Radius based on contribution count
        sqrt(node.contributionCount.toDouble()) * 5
    })
```

## Constraints
- More complex implementation than other visualizations
- Performance considerations for large teams (>20 players)
- Requires both contribution and full pairing history data
- May need to add GraphQL query for complete player/pair data if not in current viz query

## Validation
- Generate fake data with known patterns:
  - Well-connected team (dense network)
  - Siloed team (disconnected subgraphs)
  - Bridge players connecting groups
- Verify force simulation converges to stable layout
- Test drag interaction
- Confirm hover highlights work correctly
- Performance test with large team (20+ players)
- Run `./gradlew :client:check`

## Implementation Priority
**Priority: LOW** - More complex, lower immediate value than other visualizations. Recommend implementing simpler visualizations first and revisiting this if there's demand.

## Open Questions
- Should we support filtering network to show only recent pairings (e.g., last quarter)?
- Should we show solo work as self-loops or separate indicator?
- How to handle very large teams (>30 players) - zoom/pan, filtering?
