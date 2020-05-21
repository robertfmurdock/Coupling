package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.deletionResult
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdDeleteSyntax

data class DeleteTribeCommand(val tribeId: TribeId) :
    SimpleSuspendAction<DeleteTribeCommandDispatcher, Unit> {
    override val performFunc = link(DeleteTribeCommandDispatcher::perform)
}

interface DeleteTribeCommandDispatcher : TribeIdDeleteSyntax {
    suspend fun perform(command: DeleteTribeCommand) = command.tribeId.delete().deletionResult("Tribe")
}
