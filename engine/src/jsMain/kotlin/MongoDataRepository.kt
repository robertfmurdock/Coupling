import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.Promise

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

class MongoPlayerRepository(val jsRepository: dynamic) : PlayersRepository {
    override suspend fun delete(playerId: String) = suspendCancellableCoroutine<Unit> {
        jsRepository.removePlayer(playerId) { error: Json? ->
            if (error == null) {
                it.resume(Unit)
            } else {
                it.resumeWithException(Exception(message = error["message"]?.toString()))
            }
        }.unsafeCast<Unit>()
    }

    override suspend fun save(player: Player) = player.toJson()
            .apply { this["timestamp"] = Date() }
            .run { jsRepository.savePlayer(this).unsafeCast<Promise<Unit>>() }
            .await()

    override fun getPlayersAsync(tribeId: String) =
            requestJsPlayers(tribeId)
                    .then { it.map(Json::toPlayer) }
                    .asDeferred()

    private fun requestJsPlayers(tribeId: String) = jsRepository
            .requestPlayers(tribeId)
            .unsafeCast<Promise<Array<Json>>>()
}
