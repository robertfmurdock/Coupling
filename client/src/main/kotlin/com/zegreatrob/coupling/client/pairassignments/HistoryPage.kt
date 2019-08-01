package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import react.RBuilder
import kotlin.js.Promise


object HistoryPage : ComponentProvider<PageProps>(), HistoryPageBuilder

val RBuilder.historyPage get() = HistoryPage.captor(this)

private val LoadedPairAssignments = dataLoadWrapper(History)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface HistoryPageBuilder : ComponentBuilder<PageProps>, FindCallSignActionDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.pathParams["tribeId"]?.first()?.let(::TribeId)

        if (tribeId != null) {
            loadedPairAssignments(DataLoadProps { reload -> pageProps.toPairAssignmentsProps(tribeId, reload) })
        } else throw Exception("WHAT")
    }

    private suspend fun PageProps.toPairAssignmentsProps(tribeId: TribeId, reload: () -> Unit) =
            coupling.getData(tribeId)
                    .let { (tribe, history) ->
                        HistoryProps(
                                tribe = tribe,
                                history = history,
                                pathSetter = pathSetter,
                                coupling = coupling,
                                reload = reload
                        )
                    }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            Pair(getTribeAsync(tribeId), getHistoryAsync(tribeId))
                    .await()

    private suspend fun Pair<Promise<KtTribe>, Promise<List<PairAssignmentDocument>>>.await() =
            Pair(
                    first.await(),
                    second.await()
            )
}
