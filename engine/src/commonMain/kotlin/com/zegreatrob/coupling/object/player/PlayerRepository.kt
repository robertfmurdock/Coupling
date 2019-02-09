import kotlinx.coroutines.Deferred

interface PlayerRepository : PlayerGetter, PlayerSaver, PlayerDeleter

interface PlayerDeleter {
    suspend fun delete(playerId: String)
}

interface PlayerSaver {
    suspend fun save(tribeIdPlayer: TribeIdPlayer)
}

interface PlayerGetter {
    fun getPlayersAsync(tribeId: TribeId): Deferred<List<Player>>
    fun getDeletedAsync(tribeId: TribeId): Deferred<List<Player>>
}
