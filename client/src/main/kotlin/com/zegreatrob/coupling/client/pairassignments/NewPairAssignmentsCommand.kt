package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinAction
import com.zegreatrob.coupling.client.pairassignments.spin.RequestSpinActionDispatcher
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class NewPairAssignmentsCommand(val tribeId: PartyId, val playerIds: List<String>, val pinIds: List<String>) :
    SimpleSuspendAction<NewPairAssignmentsCommandDispatcher, Unit?> {
    override val performFunc = link(NewPairAssignmentsCommandDispatcher::perform)
}

interface NewPairAssignmentsCommandDispatcher : TribeIdGetSyntax,
    TribeIdPinsSyntax,
    TribeIdPlayersSyntax,
    SuspendActionExecuteSyntax,
    RequestSpinActionDispatcher,
    TribeIdPairAssignmentDocumentSaveSyntax {

    suspend fun perform(query: NewPairAssignmentsCommand) = with(query) {
        val (tribe, players, pins) = getData()
        if (tribe == null)
            null
        else {
            execute(requestSpinAction(players, pins))
                .let { tribe.id.with(it).save() }
        }
    }

    private fun NewPairAssignmentsCommand.requestSpinAction(players: List<Player>, pins: List<Pin>): RequestSpinAction {
        val selectedPlayers = filterSelectedPlayers(players, playerIds)
        val selectedPins = filterSelectedPins(pins, pinIds)
        return RequestSpinAction(tribeId, selectedPlayers, selectedPins)
    }

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

    private fun filterSelectedPins(pins: List<Pin>, pinIds: List<String>) = pins.filter { pinIds.contains(it.id) }

    private suspend fun NewPairAssignmentsCommand.getData() = coroutineScope {
        await(
            async { tribeId.get() },
            async { tribeId.getPlayerList() },
            async { tribeId.getPins() }
        )
    }

}
