package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.core.entity.tribe.TribeId

data class DeleteTribeCommand(val tribeId: TribeId) : Action

interface DeleteTribeCommandDispatcher : ActionLoggingSyntax, TribeIdDeleteSyntax {

    suspend fun DeleteTribeCommand.perform() = logAsync { tribeId.deleteAsync().await() }

}
