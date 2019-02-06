import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json
import kotlin.js.Promise

interface MongoDataRepository : CouplingDataRepository, MongoPairAssignmentDocumentRepository {

    override val jsRepository: dynamic

    override fun getPinsAsync(tribeId: String) = requestPins(tribeId)
            .then { it.toPins() }
            .asDeferred()

    private fun requestPins(tribeId: String) = jsRepository.requestPins(tribeId).unsafeCast<Promise<Array<Json>>>()

    override fun getTribeAsync(tribeId: String): Deferred<KtTribe> = requestTribe(tribeId)
            .then { it.toTribe() }
            .asDeferred()

    private fun requestTribe(tribeId: String) = jsRepository.requestTribe(tribeId).unsafeCast<Promise<Json>>()

}

