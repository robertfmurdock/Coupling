import kotlinx.coroutines.Deferred

interface CouplingDataRepository : PairAssignmentDocumentGetter {
    fun getPinsAsync(tribeId: TribeId): Deferred<List<Pin>>
    fun getTribeAsync(tribeId: TribeId): Deferred<KtTribe>
}

interface PlayerRepository {
    fun getPlayersAsync(tribeId: TribeId): Deferred<List<Player>>
    suspend fun save(tribeIdPlayer: TribeIdPlayer)
    suspend fun delete(playerId: String)
}
