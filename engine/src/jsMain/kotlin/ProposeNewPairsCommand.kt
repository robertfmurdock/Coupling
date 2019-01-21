import kotlinx.coroutines.Deferred

data class ProposeNewPairsCommand(val tribeId: String, val players: List<Player>)
interface ProposeNewPairsCommandDispatcher {

    val repository: CouplingDataRepository
    val actionDispatcher: RunGameActionDispatcher

    suspend fun ProposeNewPairsCommand.perform(): PairAssignmentDocument {
        val historyDeferred = repository.getHistory(tribeId)
        val pinsDeferred = repository.getPins(tribeId)
        val tribeDeferred = repository.getTribe(tribeId)

        val history = historyDeferred.await()
        val pins = pinsDeferred.await()
        val tribe = tribeDeferred.await()

        val action = RunGameAction(players, pins, history, tribe)

        return with(actionDispatcher) { action.perform() }
    }

}

interface CouplingDataRepository {

    fun getPins(tribeId: String): Deferred<List<Pin>>
    fun getHistory(tribeId: String): Deferred<List<HistoryDocument>>
    fun getTribe(tribeId: String): Deferred<KtTribe>

}
