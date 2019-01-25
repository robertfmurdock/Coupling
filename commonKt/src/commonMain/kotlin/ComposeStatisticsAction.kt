data class ComposeStatisticsAction(val tribe: KtTribe, val players: List<Player>, val history: List<HistoryDocument>)

interface ComposeStatisticsActionDispatcher {

    fun ComposeStatisticsAction.perform() = StatisticsReport(
            spinsUntilFullRotation = calculateFullRotation(),
            pairReports = pairReports(),
            medianSpinDuration = 0
    )

    private fun ComposeStatisticsAction.pairReports() = allPairCombinations()
            .map { PairReport(it, NeverPaired) }

    private fun ComposeStatisticsAction.allPairCombinations() =
            players.mapIndexed { index, player -> players.sliceFrom(index + 1).toPairsWith(player) }
                    .flatten()

    private fun List<Player>.sliceFrom(startIndex: Int) = slice(startIndex..lastIndex)

    private fun List<Player>.toPairsWith(player: Player) =
            map { otherPlayer -> CouplingPair.Double(player, otherPlayer) }

    private fun ComposeStatisticsAction.calculateFullRotation() = players.size.ifEvenSubtractOne()

    private fun Int.ifEvenSubtractOne() = if (this % 2 == 0) {
        this - 1
    } else {
        this
    }

}

data class StatisticsReport(
        val spinsUntilFullRotation: Int,
        val pairReports: List<PairReport>,
        val medianSpinDuration: Int
)

data class PairReport(val pair: CouplingPair, val timeSinceLastPair: TimeResult)
