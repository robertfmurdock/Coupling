package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import react.RBuilder
import kotlin.js.Promise


object StatisticsPage : ComponentProvider<PageProps>(), StatisticsPageBuilder

val RBuilder.statisticsPage get() = StatisticsPage.captor(this)

private val LoadedPairAssignments = dataLoadWrapper(TribeStatistics)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface StatisticsPageBuilder : ComponentBuilder<PageProps>, FindCallSignActionDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.pathParams["tribeId"]?.let(::TribeId)

        if (tribeId != null) {
            loadedPairAssignments(DataLoadProps { pageProps.toPairAssignmentsProps(tribeId) })
        } else throw Exception("WHAT")
    }

    private suspend fun PageProps.toPairAssignmentsProps(tribeId: TribeId) = coupling.getData(tribeId)
            .let { (tribe, players, history) ->
                TribeStatisticsProps(
                        tribe = tribe,
                        players = players,
                        history = history,
                        pathSetter = pathSetter
                )
            }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            Triple(getTribeAsync(tribeId), getPlayerListAsync(tribeId), getHistoryAsync(tribeId))
                    .await()

    private suspend fun Triple<Promise<KtTribe>, Promise<List<Player>>, Promise<List<PairAssignmentDocument>>>.await() =
            Triple(
                    first.await(),
                    second.await(),
                    third.await())
}
