import kotlinx.coroutines.Deferred

interface CouplingDataRepository {

    fun getPins(tribeId: String): Deferred<List<Pin>>
    fun getHistory(tribeId: String): Deferred<List<PairAssignmentDocument>>
    fun getTribe(tribeId: String): Deferred<KtTribe>
}