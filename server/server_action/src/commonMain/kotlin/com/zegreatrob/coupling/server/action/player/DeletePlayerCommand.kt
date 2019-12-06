package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayerId
import com.zegreatrob.coupling.model.player.TribeIdPlayerIdDeleteSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class DeletePlayerCommand(val tribeId: TribeId, val playerId: String) : Action

interface DeletePlayerCommandDispatcher : ActionLoggingSyntax, TribeIdPlayerIdDeleteSyntax {
    suspend fun DeletePlayerCommand.perform() = logAsync { TribeIdPlayerId(tribeId, playerId).run { deletePlayer() } }
}

