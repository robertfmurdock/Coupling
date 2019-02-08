import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json
import kotlin.js.Promise

interface MongoDataRepository : CouplingDataRepository, MongoPairAssignmentDocumentRepository {

    override val jsRepository: dynamic

    override fun getPinsAsync(tribeId: TribeId) = requestPins(tribeId)
            .then { it.toPins() }
            .asDeferred()

    private fun requestPins(tribeId: TribeId) = jsRepository.requestPins(tribeId.value).unsafeCast<Promise<Array<Json>>>()

    override fun getTribeAsync(tribeId: TribeId): Deferred<KtTribe> = requestTribe(tribeId)
            .then { it.toTribe() }
            .asDeferred()

    private fun requestTribe(tribeId: TribeId) = jsRepository.requestTribe(tribeId.value).unsafeCast<Promise<Json>>()

}

