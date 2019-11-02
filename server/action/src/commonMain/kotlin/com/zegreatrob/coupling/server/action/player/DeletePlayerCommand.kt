package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.PlayerDeleter

data class DeletePlayerCommand(val playerId: String) : Action

interface DeletePlayerCommandDispatcher : ActionLoggingSyntax, PlayerIdDeleteSyntax {
    suspend fun DeletePlayerCommand.perform() = logAsync { playerId.run { deletePlayer() } }
}

interface PlayerIdDeleteSyntax {
    val playerRepository: PlayerDeleter
    suspend fun String.deletePlayer() = playerRepository.deletePlayer(this)
}
