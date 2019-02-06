import kotlinx.coroutines.Deferred

interface CouplingDataRepository : PairAssignmentDocumentRepository {
    fun getPinsAsync(tribeId: String): Deferred<List<Pin>>
    fun getTribeAsync(tribeId: String): Deferred<KtTribe>
}

interface PairAssignmentDocumentRepository {
    fun getPairAssignmentsAsync(tribeId: String): Deferred<List<PairAssignmentDocument>>
}

interface PlayerRepository {
    fun getPlayersAsync(tribeId: TribeId): Deferred<List<Player>>
    suspend fun save(tribeIdPlayer: TribeIdPlayer)
    suspend fun delete(playerId: String)
}
