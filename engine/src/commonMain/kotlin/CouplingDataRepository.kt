import kotlinx.coroutines.Deferred

interface CouplingDataRepository {
    fun getPinsAsync(tribeId: String): Deferred<List<Pin>>
    fun getHistoryAsync(tribeId: String): Deferred<List<PairAssignmentDocument>>
    fun getTribeAsync(tribeId: String): Deferred<KtTribe>
}

interface PlayerRepository {
    fun getPlayersAsync(tribeId: TribeId): Deferred<List<Player>>
    suspend fun save(tribeIdPlayer: TribeIdPlayer)
    suspend fun delete(playerId: String)
}
