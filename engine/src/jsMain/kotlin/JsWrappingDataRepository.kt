import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json
import kotlin.js.Promise

@JsName("dataRepository")
fun dataRepository(jsRepository: dynamic) = JsWrappingDataRepository(jsRepository)

class JsWrappingDataRepository(val jsRepository: dynamic) : CouplingDataRepository {

    override fun getPins(tribeId: String) = requestPins(tribeId)
            .then { it.toPins() }
            .asDeferred()

    @Suppress("UnsafeCastFromDynamic")
    private fun requestPins(tribeId: String): Promise<Array<Json>> = jsRepository.requestPins(tribeId)

    override fun getHistory(tribeId: String): Deferred<List<PairAssignmentDocument>> = requestHistory(tribeId)
            .then { historyFromArray(it) }
            .asDeferred()

    @Suppress("UnsafeCastFromDynamic")
    private fun requestHistory(tribeId: String): Promise<Array<Json>> = jsRepository.requestHistory(tribeId)

    override fun getTribe(tribeId: String): Deferred<KtTribe> = requestTribe(tribeId)
            .then { it.toTribe() }
            .asDeferred()

    @Suppress("UnsafeCastFromDynamic")
    private fun requestTribe(tribeId: String): Promise<Json> = jsRepository.requestTribe(tribeId)

}