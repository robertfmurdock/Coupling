package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.ComposeStatisticsAction
import com.zegreatrob.coupling.action.ComposeStatisticsActionDispatcher
import com.zegreatrob.coupling.action.StatisticsReport
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class StatisticsQuery(val tribeId: PartyId) :
    SimpleSuspendAction<StatisticsQueryDispatcher, StatisticQueryResults?> {
    override val performFunc = link(StatisticsQueryDispatcher::perform)
}

data class StatisticQueryResults(
    val tribe: Party,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
    val report: StatisticsReport,
    val heatmapData: List<List<Double?>>
)

interface StatisticsQueryDispatcher : ExecutableActionExecuteSyntax,
    ComposeStatisticsActionDispatcher,
    CalculateHeatMapActionDispatcher,
    TribeIdLoadAllSyntax {

    suspend fun perform(query: StatisticsQuery) = query.loadAll()

    private suspend fun StatisticsQuery.loadAll() = tribeId.loadAll()?.let { (tribe, players, history) ->
        val (report, heatmapData) = calculateStats(tribe, players, history)
        StatisticQueryResults(tribe, players, history, report, heatmapData)
    }

    private fun calculateStats(tribe: Party, players: List<Player>, history: List<PairAssignmentDocument>)
            : Pair<StatisticsReport, List<List<Double?>>> {
        val statisticsReport = composeStatistics(tribe, players, history)
        return statisticsReport to calculateHeatMap(players, history, statisticsReport)
    }

    private fun composeStatistics(tribe: Party, players: List<Player>, history: List<PairAssignmentDocument>) =
        execute(ComposeStatisticsAction(tribe, players, history))

    private fun calculateHeatMap(
        players: List<Player>,
        history: List<PairAssignmentDocument>,
        statisticsResult: StatisticsReport
    ) = execute(
        CalculateHeatMapAction(players, history, statisticsResult.spinsUntilFullRotation)
    )

}
