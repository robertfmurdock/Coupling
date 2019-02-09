package com.zegreatrob.coupling.entity.player

data class DeletePlayerCommand(val playerId: String)

interface DeletePlayerCommandDispatcher : PlayerIdDeleteSyntax {
    suspend fun DeletePlayerCommand.perform() = playerId.apply { deletePlayer() }
}

interface PlayerIdDeleteSyntax {
    val playerRepository: PlayerDeleter
    suspend fun String.deletePlayer() = playerRepository.delete(this)
}
