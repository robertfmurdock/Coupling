import kotlinx.coroutines.Deferred

interface CouplingDataRepository {

    fun getPlayersAsync(tribeId: String) : Deferred<List<Player>>
    fun getPinsAsync(tribeId: String): Deferred<List<Pin>>
    fun getHistoryAsync(tribeId: String): Deferred<List<PairAssignmentDocument>>
    fun getTribeAsync(tribeId: String): Deferred<KtTribe>
}