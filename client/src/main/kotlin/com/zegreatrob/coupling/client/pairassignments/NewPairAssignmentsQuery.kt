package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinAction
import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class NewPairAssignmentsQuery(val tribeId: TribeId, val playerIds: List<String>, val pinIds: List<String>) :
    SimpleSuspendAction<NewPairAssignmentsQueryDispatcher, Triple<Tribe?, List<Player>, PairAssignmentDocument>> {
    override val performFunc = link(NewPairAssignmentsQueryDispatcher::perform)
}

interface NewPairAssignmentsQueryDispatcher : TribeIdGetSyntax,
    TribeIdPinsSyntax,
    TribeIdPlayersSyntax,
    DispatchSyntax,
    RequestSpinActionDispatcher {
    suspend fun perform(query: NewPairAssignmentsQuery) = with(query) {
        val (tribe, players, pins) = getData()
        val selectedPlayers = filterSelectedPlayers(players, playerIds)
        val selectedPins = filterSelectedPins(pins, pinIds)
        val pairAssignmentsResult = performSpin(tribeId, selectedPlayers, selectedPins)

        verifySuccess(pairAssignmentsResult, tribe, players)
    }

    private fun verifySuccess(
        pairAssignmentsResult: Result<PairAssignmentDocument>,
        tribe: Tribe?,
        players: List<Player>
    ) = when (pairAssignmentsResult) {
        is SuccessfulResult -> Triple(tribe, players, pairAssignmentsResult.value)
            .successResult()
        else -> throw Exception("Unhandled failure path")
    }

    private suspend fun performSpin(tribeId: TribeId, players: List<Player>, pins: List<Pin>) =
        execute(RequestSpinAction(tribeId, players, pins))

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

    private fun filterSelectedPins(pins: List<Pin>, pinIds: List<String>) = pins.filter { pinIds.contains(it._id) }

    private suspend fun NewPairAssignmentsQuery.getData() = coroutineScope {
        with(tribeId) {
            await(
                async { get() },
                async { getPlayerList() },
                async { getPins() }
            )
        }
    }

}