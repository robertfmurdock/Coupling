data class DeletePlayerCommand(val playerId: String)

interface DeletePlayerCommandDispatcher : PlayerIdDeleteSyntax {
    suspend fun DeletePlayerCommand.perform() = playerId.apply { deletePlayer() }
}

interface PlayerIdDeleteSyntax {
    val repository: PlayersRepository
    suspend fun String.deletePlayer() = repository.delete(this)
}
