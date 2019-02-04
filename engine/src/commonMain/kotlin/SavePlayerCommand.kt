data class SavePlayerCommand(val player: Player)

interface SavePlayerCommandDispatcher {

    val repository: PlayersRepository

    suspend fun SavePlayerCommand.perform() = player.apply { repository.save(this) }
}
