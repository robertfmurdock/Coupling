package com.zegreatrob.coupling.client.memory

import com.zegreatrob.coupling.action.stats.ComposeStatisticsAction
import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.action.stats.StatisticsReport
import com.zegreatrob.coupling.action.stats.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class ClientStatisticsAction(
    val party: PartyDetails,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
    val pairs: List<PlayerPair>,
) : SimpleSuspendAction<ClientStatisticsAction.Dispatcher, StatisticsQuery.Results> {
    override val performFunc = link(Dispatcher::calculate)

    interface Dispatcher :
        ComposeStatisticsAction.Dispatcher,
        CalculateHeatMapAction.Dispatcher {

        fun calculate(query: ClientStatisticsAction): StatisticsQuery.Results = with(query) {
            val (report, heatmapData) = calculateStats(party, players, history)
            return StatisticsQuery.Results(party, players, history, query.pairs, report, heatmapData)
        }

        private fun calculateStats(
            party: PartyDetails,
            players: List<Player>,
            history: List<PairAssignmentDocument>,
        ): Pair<StatisticsReport, List<List<Double?>>> {
            val statisticsReport = composeStatistics(party, players, history)
            return statisticsReport to calculateHeatMap(players, history, statisticsReport)
        }

        private fun composeStatistics(
            party: PartyDetails,
            players: List<Player>,
            history: List<PairAssignmentDocument>,
        ) = execute(ComposeStatisticsAction(party, players, history))

        private fun calculateHeatMap(
            players: List<Player>,
            history: List<PairAssignmentDocument>,
            statisticsResult: StatisticsReport,
        ) = execute(
            CalculateHeatMapAction(players, history, statisticsResult.spinsUntilFullRotation),
        )
    }
}
