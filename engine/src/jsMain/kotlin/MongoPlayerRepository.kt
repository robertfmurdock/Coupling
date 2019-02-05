import com.soywiz.klock.internal.toDateTime
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.await
import kotlin.js.*

class MongoPlayerRepository(val jsRepository: dynamic) : PlayersRepository {

    val playersCollection: dynamic = jsRepository.playersCollection

    override suspend fun delete(playerId: String) = getPlayerJsonHistory(playerId).toList()
            .latestByTimestamp()
            ?.apply { this["timestamp"] = Date(); this["isDeleted"] = true }
            ?.run { jsRepository.savePlayer(this).unsafeCast<Promise<Unit>>().await() }
            ?: throw Exception(message = "Failed to remove the player because it did not exist.")

    private suspend fun getPlayerJsonHistory(playerId: String) =
            playersCollection.find(json("id" to playerId)).unsafeCast<Promise<Array<Json>>>().await() +
                    playersCollection.find(json("_id" to playerId)).unsafeCast<Promise<Array<Json>>>().await()

    override suspend fun save(player: Player) = player.toJson()
            .apply {
                this["id"] = player._id
                this["_id"] = null
                this["timestamp"] = Date()
            }
            .run { jsRepository.savePlayer(this).unsafeCast<Promise<Unit>>() }
            .await()

    override fun getPlayersAsync(tribeId: String) =
            requestJsPlayers(tribeId)
                    .then { jsonArray -> jsonArray.dbJsonToPlayers() }
                    .asDeferred()

    private fun Array<Json>.dbJsonToPlayers() = map { it.apply { this["_id"] = this["id"] ?: this["_id"] } }
            .groupBy { it["_id"].toString() }
            .map { it.value.latestByTimestamp() }
            .filterNotNull()
            .filter { it["isDeleted"] != true }
            .map(Json::toPlayer)

    private fun List<Json>.latestByTimestamp() = sortedByDescending { json -> json.timeStamp() }.firstOrNull()

    private fun Json.timeStamp() = this["timestamp"]?.unsafeCast<Date>()?.toDateTime()

    private fun requestJsPlayers(tribeId: String) = jsRepository
            .requestPlayers(tribeId)
            .unsafeCast<Promise<Array<Json>>>()
}