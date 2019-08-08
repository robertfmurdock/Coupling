package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.sdk.GetPairAssignmentListSyntax
import com.zegreatrob.coupling.client.sdk.GetPlayerListSyntax
import com.zegreatrob.coupling.client.sdk.GetTribeSyntax
import com.zegreatrob.coupling.common.*
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapActionDispatcher
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

data class StatisticsQuery(val tribeId: TribeId) : Action

data class StatisticQueryResults(
    val tribe: KtTribe,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
    val report: StatisticsReport,
    val heatmapData: List<List<Double?>>
)

interface StatisticsQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetPlayerListSyntax,
    GetPairAssignmentListSyntax,
    ComposeStatisticsActionDispatcher,
    CalculateHeatMapActionDispatcher {

    suspend fun StatisticsQuery.perform() = logAsync {
        val (tribe, players, history) = tribeId.getData()

        val (report, heatmapData) = calculateStats(tribe, players, history)

        StatisticQueryResults(tribe, players, history, report, heatmapData)
    }

    private suspend fun TribeId.getData() =
        Triple(
            getTribeAsync(),
            getPlayerListAsync(),
            getPairAssignmentListAsync()
        ).await()

    private suspend fun Triple<Deferred<KtTribe>, Deferred<List<Player>>, Deferred<List<PairAssignmentDocument>>>.await() =
        Triple(
            first.await(),
            second.await(),
            third.await()
        )

    private fun calculateStats(
        tribe: KtTribe,
        players: List<Player>,
        history: List<PairAssignmentDocument>
    ) = ComposeStatisticsAction(tribe, players, history).perform() to
            CalculateHeatMapAction(
                players,
                history,
                ComposeStatisticsAction(tribe, players, history).perform().spinsUntilFullRotation
            )
                .perform()
}

