package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdDeleteSyntax

data class DeletePartyCommand(val partyId: PartyId) :
    SimpleSuspendResultAction<DeletePartyCommandDispatcher, Unit> {
    override val performFunc = link(DeletePartyCommandDispatcher::perform)
}

interface DeletePartyCommandDispatcher : PartyIdDeleteSyntax {
    suspend fun perform(command: DeletePartyCommand) = command.partyId.deleteIt().deletionResult("tribe")
}
