import kotlinx.coroutines.Deferred

interface CouplingDataRepository : PairAssignmentDocumentGetter {
    fun getPinsAsync(tribeId: String): Deferred<List<Pin>>
    fun getTribeAsync(tribeId: String): Deferred<KtTribe>
}

interface PairAssignmentDocumentRepository : PairAssignmentDocumentSaver, PairAssignmentDocumentGetter, PairAssignmentDocumentDeleter

interface PairAssignmentDocumentDeleter {
    suspend fun delete(pairAssignmentDocumentId: PairAssignmentDocumentId)
}

interface PairAssignmentDocumentGetter {
    fun getPairAssignmentsAsync(tribeId: String): Deferred<List<PairAssignmentDocument>>
}

interface PairAssignmentDocumentSaver {
    suspend fun save(pairAssignmentDocument: PairAssignmentDocument)
}

interface PlayerRepository {
    fun getPlayersAsync(tribeId: TribeId): Deferred<List<Player>>
    suspend fun save(tribeIdPlayer: TribeIdPlayer)
    suspend fun delete(playerId: String)
}
