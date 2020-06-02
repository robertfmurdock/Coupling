package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax

data class StatisticsQuery(val tribeId: TribeId) :
    SimpleSuspendResultAction<StatisticsQueryDispatcher, StatisticQueryResults> {
    override val performFunc = link(StatisticsQueryDispatcher::perform)
}

data class StatisticQueryResults(
    val tribe: Tribe,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
    val report: StatisticsReport,
    val heatmapData: List<List<Double?>>
)

interface StatisticsQueryDispatcher : ExecutableActionExecuteSyntax,
    ComposeStatisticsActionDispatcher,
    CalculateHeatMapActionDispatcher,
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

    private fun composeStatistics(tribe: Tribe, players: List<Player>, history: List<PairAssignmentDocument>) =
        execute(ComposeStatisticsAction(tribe, players, history))

    private fun calculateHeatMap(
        players: List<Player>,
        history: List<PairAssignmentDocument>,
        statisticsResult: StatisticsReport
    ) = execute(
        CalculateHeatMapAction(players, history, statisticsResult.spinsUntilFullRotation)
    )

}

