package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdDeleteSyntax

data class DeleteTribeCommand(val tribeId: TribeId) : SimpleSuspendAction<DeleteTribeCommandDispatcher, Unit> {
    override val performFunc = link(DeleteTribeCommandDispatcher::perform)
}

interface DeleteTribeCommandDispatcher : TribeIdDeleteSyntax {
    suspend fun perform(command: DeleteTribeCommand) = command.tribeId.delete().deletionResult("tribe")
}
