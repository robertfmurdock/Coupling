import kotlinx.coroutines.Deferred

interface CouplingDataRepository : PairAssignmentDocumentGetter {
    fun getPinsAsync(tribeId: TribeId): Deferred<List<Pin>>
    fun getTribeAsync(tribeId: TribeId): Deferred<KtTribe>
}

