package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import react.RBuilder
import kotlin.js.Promise


object NewPairAssignmentsPage : ComponentProvider<PageProps>(), NewPairAssignmentsPageBuilder

val RBuilder.newPairAssignmentsPage get() = NewPairAssignmentsPage.captor(this)

private val LoadedPairAssignments = dataLoadWrapper(PairAssignments)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.captor(this)

interface NewPairAssignmentsPageBuilder : ComponentBuilder<PageProps>, FindCallSignActionDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.pathParams["tribeId"]?.first()?.let(::TribeId)

        if (tribeId != null) {
            loadedPairAssignments(DataLoadProps { pageProps.toPairAssignmentsProps(tribeId) })
        } else throw Exception("WHAT")
    }

    private suspend fun PageProps.toPairAssignmentsProps(tribeId: TribeId) = getData(tribeId)
            .let { (tribe, players, pairAssignments) ->
                PairAssignmentsProps(
                        tribe = tribe,
                        players = players,
                        pairAssignments = pairAssignments,
                        pathSetter = pathSetter,
                        coupling = coupling
                )
            }

    private suspend fun PageProps.getData(tribeId: TribeId) = coupling.getData(tribeId)
            .let { (tribe, players) ->
                val pairAssignments = performSpin(players, tribeId)
                Triple(tribe, players, pairAssignments)
            }

    private suspend fun PageProps.performSpin(players: List<Player>, tribeId: TribeId): PairAssignmentDocument {
        val playerIds = search.getAll("player")

        val selectedPlayers = players.filter { playerIds.contains(it.id) }

        return coupling.spinAsync(selectedPlayers, tribeId).await()
    }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            Pair(getTribeAsync(tribeId), getPlayerListAsync(tribeId))
                    .await()

    private suspend fun Pair<Promise<KtTribe>, Promise<List<Player>>>.await() =
            Pair(
                    first.await(),
                    second.await()
            )
}

