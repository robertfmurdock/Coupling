package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

data class StatisticsQuery(val tribeId: TribeId) :
    SimpleSuspendAction<StatisticsQueryDispatcher, StatisticQueryResults> {
    override val perform = link(StatisticsQueryDispatcher::perform)
}

data class StatisticQueryResults(
    val tribe: Tribe,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
    val report: StatisticsReport,
    val heatmapData: List<List<Double?>>
)

interface StatisticsQueryDispatcher : ComposeStatisticsActionDispatcher, CalculateHeatMapActionDispatcher,
    TribeIdLoadAllSyntax {

    suspend fun perform(query: StatisticsQuery) = query.loadAll().successResult()

    private suspend fun StatisticsQuery.loadAll(): StatisticQueryResults {
        val (tribe, players, history) = tribeId.loadAll()

        val (report, heatmapData) = calculateStats(tribe, players, history)

        return StatisticQueryResults(tribe, players, history, report, heatmapData)
    }

    private fun calculateStats(
        tribe: Tribe,
        players: List<Player>,
        history: List<PairAssignmentDocument>
    ) = composeStatistics(tribe, players, history).let { statisticsResult ->
        statisticsResult to calculateHeatMap(players, history, statisticsResult)
    }

    private fun calculateHeatMap(
        players: List<Player>,
        history: List<PairAssignmentDocument>,
        statisticsResult: StatisticsReport
    ) = CalculateHeatMapAction(
        players,
        history,
        statisticsResult.spinsUntilFullRotation
    )
        .perform()

    private fun composeStatistics(
        tribe: Tribe,
        players: List<Player>,
        history: List<PairAssignmentDocument>
    ) = perform(ComposeStatisticsAction(tribe, players, history)).value
}

