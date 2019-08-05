package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getHistoryAsync
import com.zegreatrob.coupling.client.getPlayerListAsync
import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.*
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommand
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommandDispatcher
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.await
import kotlin.js.Promise

data class StatisticsQuery(val tribeId: TribeId, val coupling: Coupling) : Action

data class StatisticQueryResults(
        val tribe: KtTribe,
        val players: List<Player>,
        val history: List<PairAssignmentDocument>,
        val report: StatisticsReport,
        val heatmapData: List<List<Double?>>
)

interface StatisticsQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, ComposeStatisticsActionDispatcher,
        CalculateHeatMapCommandDispatcher {

    suspend fun StatisticsQuery.perform() = logAsync {
        val (tribe, players, history) = coupling.getData(tribeId)

        val (report, heatmapData) = calculateStats(tribe, players, history)

        StatisticQueryResults(tribe, players, history, report, heatmapData)
    }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            Triple(
                    getTribeAsync(tribeId),
                    getPlayerListAsync(tribeId),
                    getHistoryAsync(tribeId)
            ).await()

    private suspend fun Triple<Deferred<KtTribe>, Promise<List<Player>>, Promise<List<PairAssignmentDocument>>>.await() =
            Triple(
                    first.await(),
                    second.await(),
                    third.await()
            )

    private fun calculateStats(
            tribe: KtTribe,
            players: List<Player>,
            history: List<PairAssignmentDocument>
    ): Pair<StatisticsReport, List<List<Double?>>> {
        return ComposeStatisticsAction(tribe, players, history).perform() to
                CalculateHeatMapCommand(players, history, ComposeStatisticsAction(tribe, players, history).perform().spinsUntilFullRotation)
                        .perform()
    }
}

