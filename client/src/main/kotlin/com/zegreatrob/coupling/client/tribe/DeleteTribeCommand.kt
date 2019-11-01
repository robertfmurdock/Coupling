package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class DeleteTribeCommand(val tribeId: TribeId) : Action

interface DeleteTribeCommandDispatcher : ActionLoggingSyntax, TribeIdDeleteSyntax {

    suspend fun DeleteTribeCommand.perform() = logAsync { tribeId.deleteAsync().await() }

}
