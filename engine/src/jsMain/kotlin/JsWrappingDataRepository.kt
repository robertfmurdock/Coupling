import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json
import kotlin.js.Promise

@JsName("dataRepository")
fun dataRepository(jsRepository: dynamic) = JsWrappingDataRepository(jsRepository)

class JsWrappingDataRepository(private val jsRepository: dynamic) : CouplingDataRepository {

    override fun getPlayersAsync(tribeId: String) =
            requestJsPlayers(tribeId)
                    .then { it.map(Json::toPlayer) }
                    .asDeferred()

    private fun requestJsPlayers(tribeId: String) = jsRepository
            .requestPlayers(tribeId)
            .unsafeCast<Promise<List<Json>>>()

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