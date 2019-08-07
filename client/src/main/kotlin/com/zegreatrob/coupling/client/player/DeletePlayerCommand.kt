package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.sdk.PlayerDeleteSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class DeletePlayerCommand(val tribeId: TribeId, val playerId: String) : Action

interface DeletePlayerCommandDispatcher : ActionLoggingSyntax, PlayerDeleteSyntax {

    suspend fun DeletePlayerCommand.perform() = logAsync { deleteAsync(tribeId, playerId).await() }

}