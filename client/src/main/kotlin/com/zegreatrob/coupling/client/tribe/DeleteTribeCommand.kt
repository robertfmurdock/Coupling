package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdDeleteSyntax

data class DeleteTribeCommand(val tribeId: TribeId) : SuspendAction<DeleteTribeCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: DeleteTribeCommandDispatcher) = with(dispatcher) { perform() }
}

interface DeleteTribeCommandDispatcher : TribeIdDeleteSyntax {
    suspend fun DeleteTribeCommand.perform() = tribeId.delete().deletionResult("tribe")
}
