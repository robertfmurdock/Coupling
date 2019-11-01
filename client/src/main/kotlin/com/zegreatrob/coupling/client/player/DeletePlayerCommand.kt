package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.sdk.PlayerDeleteSyntax
import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class DeletePlayerCommand(val tribeId: TribeId, val playerId: String) : Action

interface DeletePlayerCommandDispatcher : ActionLoggingSyntax, PlayerDeleteSyntax {

    suspend fun DeletePlayerCommand.perform() = logAsync { deleteAsync(tribeId, playerId).await() }

}