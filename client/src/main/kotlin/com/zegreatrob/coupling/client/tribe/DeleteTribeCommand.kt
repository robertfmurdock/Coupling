package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdDeleteSyntax

data class DeleteTribeCommand(val tribeId: PartyId) :
    SimpleSuspendResultAction<DeleteTribeCommandDispatcher, Unit> {
    override val performFunc = link(DeleteTribeCommandDispatcher::perform)
}

interface DeleteTribeCommandDispatcher : PartyIdDeleteSyntax {
    suspend fun perform(command: DeleteTribeCommand) = command.tribeId.delete().deletionResult("tribe")
}
