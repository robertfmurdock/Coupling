package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.repository.tribe.TribeIdDeleteSyntax

object DeleteTribeCommand : SimpleSuspendResultAction<DeleteTribeCommandDispatcher, Unit> {
    override val performFunc = link(DeleteTribeCommandDispatcher::perform)
}

interface DeleteTribeCommandDispatcher : TribeIdDeleteSyntax, CurrentTribeIdSyntax {
    suspend fun perform(command: DeleteTribeCommand) = currentTribeId.delete().deletionResult("Tribe")
}
