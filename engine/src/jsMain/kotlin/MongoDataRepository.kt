import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json
import kotlin.js.Promise

fun dataRepository(jsRepository: dynamic, username: String): MongoDataRepository = object : MongoDataRepository {
    override val jsRepository: dynamic = jsRepository
    override val userContext: UserContext = object : UserContext {
        override val username = username
    }
}

interface MongoDataRepository : CouplingDataRepository, MongoPlayerRepository {

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

