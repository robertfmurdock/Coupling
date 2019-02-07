import kotlinx.coroutines.Deferred

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