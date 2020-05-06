package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdDeleteSyntax

data class DeleteTribeCommand(val tribeId: TribeId) : Action

interface DeleteTribeCommandDispatcher : ActionLoggingSyntax, TribeIdDeleteSyntax {
    suspend fun DeleteTribeCommand.perform() = logAsync { tribeId.delete() }
}
