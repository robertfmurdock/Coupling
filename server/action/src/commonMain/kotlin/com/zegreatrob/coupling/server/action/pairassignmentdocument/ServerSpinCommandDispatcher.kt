package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPinRecordsSyntax
import com.zegreatrob.coupling.repository.party.PartyIdLoadSyntax
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersSyntax
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

interface ServerSpinCommandDispatcher :
    SpinCommand.Dispatcher,
    SpinAction.Dispatcher,
    SuspendActionExecuteSyntax,
    PartyIdPairAssignmentDocumentSaveSyntax,
    PartyIdLoadSyntax,
    PartyIdLoadPlayersSyntax,
    PartyIdPinRecordsSyntax {

    override suspend fun perform(command: SpinCommand): VoidResult = with(command) {
        val (party, players, pins) = getData()
        if (party == null) {
            VoidResult.Rejected
        } else {
            execute(requestSpinAction(players, pins))
                ?.let { party.id.with(it).save() }
                ?.let { VoidResult.Accepted }
                ?: VoidResult.Rejected
        }
    }

    private fun SpinCommand.requestSpinAction(players: List<Player>, pins: List<Pin>) = SpinAction(
        partyId = partyId,
        players = filterSelectedPlayers(players, playerIds),
        pins = filterSelectedPins(pins, pinIds),
    )

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

    private fun filterSelectedPins(pins: List<Pin>, pinIds: List<String>) = pins.filter { pinIds.contains(it.id) }

    private suspend fun SpinCommand.getData() = Triple(
        partyId.load()?.data,
        partyId.loadPlayers().elements,
        partyId.loadPins().elements,
    )
}
