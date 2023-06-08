package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.pairassignmentdocument.NewPairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.SdkProviderSyntax
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

interface ClientNewPairAssignmentsCommandDispatcher :
    NewPairAssignmentsCommand.Dispatcher,
    SdkProviderSyntax,
    SuspendActionExecuteSyntax,
    SpinCommand.Dispatcher {

    override suspend fun perform(query: NewPairAssignmentsCommand) = with(query) {
        val (party, players, pins) = getData()
        if (party == null) {
            null
        } else {
            execute(requestSpinAction(players, pins))
                .let { sdk.perform(SavePairAssignmentsCommand(party.id, it)) }
                .let { Unit }
        }
    }

    private fun NewPairAssignmentsCommand.requestSpinAction(players: List<Player>, pins: List<Pin>): SpinCommand {
        val selectedPlayers = filterSelectedPlayers(players, playerIds)
        val selectedPins = filterSelectedPins(pins, pinIds)
        return SpinCommand(partyId, selectedPlayers, selectedPins)
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
