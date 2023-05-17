package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.NewPairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

interface ClientNewPairAssignmentsCommandDispatcher :
    NewPairAssignmentsCommand.Dispatcher,
    SdkProviderSyntax,
    SuspendActionExecuteSyntax,
    RequestSpinAction.Dispatcher,
    PartyIdPairAssignmentDocumentSaveSyntax {

    override suspend fun perform(query: NewPairAssignmentsCommand) = with(query) {
        val (party, players, pins) = getData()
        if (party == null) {
            null
        } else {
            execute(requestSpinAction(players, pins))
                .let { party.id.with(it).save() }
        }
    }

    private fun NewPairAssignmentsCommand.requestSpinAction(players: List<Player>, pins: List<Pin>): RequestSpinAction {
        val selectedPlayers = filterSelectedPlayers(players, playerIds)
        val selectedPins = filterSelectedPins(pins, pinIds)
        return RequestSpinAction(partyId, selectedPlayers, selectedPins)
    }

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

    private fun filterSelectedPins(pins: List<Pin>, pinIds: List<String>) = pins.filter { pinIds.contains(it.id) }

    private suspend fun NewPairAssignmentsCommand.getData() = sdk.perform(
        graphQuery {
            party(partyId) {
                party()
                playerList()
                pinList()
            }
        },
    )?.partyData.let {
        Triple(it?.party?.data, it?.playerList?.elements ?: emptyList(), it?.pinList?.elements ?: emptyList())
    }
}
