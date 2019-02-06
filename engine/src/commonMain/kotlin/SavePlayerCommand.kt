data class SavePlayerCommand(val player: Player, val tribeId: TribeId)

interface SavePlayerCommandDispatcher {

    val repository: PlayersRepository

    suspend fun SavePlayerCommand.perform() = player.apply { repository.save(this, tribeId) }
}
