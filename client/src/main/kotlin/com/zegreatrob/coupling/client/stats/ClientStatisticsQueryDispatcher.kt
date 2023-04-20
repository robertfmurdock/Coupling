package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.stats.ComposeStatisticsAction
import com.zegreatrob.coupling.action.stats.ComposeStatisticsActionDispatcher
import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.action.stats.StatisticsReport
import com.zegreatrob.coupling.action.stats.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.action.stats.heatmap.CalculateHeatMapActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax

interface ClientStatisticsQueryDispatcher :
    ExecutableActionExecuteSyntax,
    ComposeStatisticsActionDispatcher,
    CalculateHeatMapActionDispatcher,
    PartyLoadAllSyntax,
    StatisticsQuery.Dispatcher {

    override suspend fun perform(query: StatisticsQuery) = query.loadAll()

    private suspend fun StatisticsQuery.loadAll() = partyId.loadAll()?.let { (party, players, history) ->
        val (report, heatmapData) = calculateStats(party, players, history)
        StatisticsQuery.Results(party, players, history, report, heatmapData)
    }

    private fun calculateStats(
        party: Party,
        players: List<Player>,
        history: List<PairAssignmentDocument>,
    ): Pair<StatisticsReport, List<List<Double?>>> {
        val statisticsReport = composeStatistics(party, players, history)
        return statisticsReport to calculateHeatMap(players, history, statisticsReport)
    }

    private fun composeStatistics(party: Party, players: List<Player>, history: List<PairAssignmentDocument>) =
        execute(ComposeStatisticsAction(party, players, history))

    private fun calculateHeatMap(
        players: List<Player>,
        history: List<PairAssignmentDocument>,
        statisticsResult: StatisticsReport,
    ) = execute(
        CalculateHeatMapAction(players, history, statisticsResult.spinsUntilFullRotation),
    )
}
