package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
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
    SuspendAction<NewPairAssignmentsQueryDispatcher, Triple<Tribe?, List<Player>, PairAssignmentDocument>> {

    override suspend fun execute(dispatcher: NewPairAssignmentsQueryDispatcher) = with(dispatcher) { perform() }
}

interface NewPairAssignmentsQueryDispatcher : TribeIdGetSyntax,
    TribeIdPinsSyntax,
    TribeIdPlayersSyntax,
    RequestSpinActionDispatcher {
    suspend fun NewPairAssignmentsQuery.perform(): SuccessfulResult<Triple<Tribe?, List<Player>, PairAssignmentDocument>> {
        val (tribe, players, pins) = getData()
        val selectedPlayers = filterSelectedPlayers(players, playerIds)
        val selectedPins = filterSelectedPins(pins, pinIds)
        val pairAssignments = performSpin(tribeId, selectedPlayers, selectedPins)
        return Triple(tribe, players, pairAssignments)
            .successResult()
    }

    private suspend fun performSpin(tribeId: TribeId, players: List<Player>, pins: List<Pin>) =
        RequestSpinAction(tribeId, players, pins)
            .perform()

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