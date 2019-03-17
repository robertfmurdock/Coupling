package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax

data class DeletePlayerCommand(val playerId: String) : Action

interface DeletePlayerCommandDispatcher : ActionLoggingSyntax, PlayerIdDeleteSyntax {
    suspend fun DeletePlayerCommand.perform() = logAsync { playerId.run { deletePlayer() } }
}

interface PlayerIdDeleteSyntax {
    val playerRepository: PlayerDeleter
    suspend fun String.deletePlayer() = playerRepository.delete(this)
}
