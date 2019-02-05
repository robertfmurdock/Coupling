import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.*

fun dataRepository(jsRepository: dynamic) = MongoDataRepository(jsRepository)

class MongoDataRepository(private val jsRepository: dynamic) : CouplingDataRepository,
        PlayersRepository by MongoPlayerRepository(jsRepository) {

    override fun getPinsAsync(tribeId: String) = requestPins(tribeId)
            .then { it.toPins() }
            .asDeferred()

    private fun requestPins(tribeId: String) = jsRepository.requestPins(tribeId).unsafeCast<Promise<Array<Json>>>()

    override fun getHistoryAsync(tribeId: String): Deferred<List<PairAssignmentDocument>> = requestHistory(tribeId)
            .then { historyFromArray(it) }
            .asDeferred()

    private fun requestHistory(tribeId: String) = jsRepository.requestHistory(tribeId).unsafeCast<Promise<Array<Json>>>()

    override fun getTribeAsync(tribeId: String): Deferred<KtTribe> = requestTribe(tribeId)
            .then { it.toTribe() }
            .asDeferred()

    private fun requestTribe(tribeId: String) = jsRepository.requestTribe(tribeId).unsafeCast<Promise<Json>>()

}

