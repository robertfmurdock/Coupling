data class ComposeStatisticsAction(val tribe: KtTribe, val players: List<Player>, val history: List<HistoryDocument>)

interface ComposeStatisticsActionDispatcher {

    fun ComposeStatisticsAction.perform() = StatisticsReport(
            spinsUntilFullRotation = calculateFullRotation(),
            pairReports = emptyList(),
            medianSpinDuration = 0
    )

    private fun ComposeStatisticsAction.calculateFullRotation() = players.size.ifEvenSubtractOne()

    private fun Int.ifEvenSubtractOne() = if (this % 2 == 0) {
        this - 1
    } else {
        this
    }

}

data class StatisticsReport(
        val spinsUntilFullRotation: Int,
        val pairReports: List<Any>,
        val medianSpinDuration: Int
)