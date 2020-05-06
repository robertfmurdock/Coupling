package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdDeleteSyntax

data class DeleteTribeCommand(val tribeId: TribeId) : SuspendAction<DeleteTribeCommandDispatcher, Boolean> {
    override suspend fun execute(dispatcher: DeleteTribeCommandDispatcher): Boolean = with(dispatcher) { perform() }
}

interface DeleteTribeCommandDispatcher : TribeIdDeleteSyntax {
    suspend fun DeleteTribeCommand.perform() = tribeId.delete()
}
