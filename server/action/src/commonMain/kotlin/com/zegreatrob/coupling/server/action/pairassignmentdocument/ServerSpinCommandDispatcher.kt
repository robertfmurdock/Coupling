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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface ServerSpinCommandDispatcher :
    SpinCommand.Dispatcher,
    RunGameAction.Dispatcher,
    SuspendActionExecuteSyntax,
    PartyIdPairAssignmentDocumentSaveSyntax,
    PartyIdLoadSyntax,
    PartyIdLoadPlayersSyntax,
    PartyIdHistorySyntax,
    PartyIdPinRecordsSyntax {

    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

    override suspend fun perform(command: SpinCommand): VoidResult = with(command) {
        runGameAction(partyId)
            ?.let { execute(it) }
            ?.let { partyId.with(it).save() }
            ?.let { VoidResult.Accepted }
            ?: VoidResult.Rejected
    }

    private suspend fun SpinCommand.runGameAction(partyId: PartyId): RunGameAction? = coroutineScope {
        val partyDeferred = async { partyId.load()?.data }
        val playersDeferred = async { partyId.loadPlayers().elements }
        val pinsDeferred = async { partyId.loadPins().elements }
        val historyDeferred = async { partyId.loadHistory() }
        RunGameAction(
            party = partyDeferred.await()
                ?: return@coroutineScope null,
            players = filterSelectedPlayers(playersDeferred.await(), playerIds),
            pins = filterSelectedPins(pinsDeferred.await(), pinIds),
            history = historyDeferred.await(),
        )
    }

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

    private fun filterSelectedPins(pins: List<Pin>, pinIds: List<String>) = pins.filter { pinIds.contains(it.id) }
}
