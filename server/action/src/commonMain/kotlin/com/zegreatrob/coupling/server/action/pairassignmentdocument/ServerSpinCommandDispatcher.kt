package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistorySyntax
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
    PartyIdHistorySyntax,
    PartyIdPinRecordsSyntax {

    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

    override suspend fun perform(command: SpinCommand): VoidResult = with(command) {
        requestSpinAction(partyId)
            ?.let { execute(it) }
            ?.let { partyId.with(it).save() }
            ?.let { VoidResult.Accepted }
            ?: VoidResult.Rejected
    }

    private suspend fun SpinCommand.requestSpinAction(partyId: PartyId): SpinAction? {
        return SpinAction(
            party = partyId.load()?.data ?: return null,
            players = filterSelectedPlayers(partyId.loadPlayers().elements, playerIds),
            pins = filterSelectedPins(partyId.loadPins().elements, pinIds),
            history = partyId.loadHistory(),
        )
    }

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

    private fun filterSelectedPins(pins: List<Pin>, pinIds: List<String>) = pins.filter { pinIds.contains(it.id) }
}
