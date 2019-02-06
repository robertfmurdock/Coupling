import com.soywiz.klock.internal.toDateTime
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.js.*

interface MongoPlayerRepository : PlayerRepository, UserContextSyntax {

    val jsRepository: dynamic

    val playersCollection: dynamic get() = jsRepository.playersCollection

    override suspend fun delete(playerId: String) = getPlayerJsonHistory(playerId).toList()
            .latestByTimestamp()
            .let { it ?: throw Exception(message = "Failed to remove the player because it did not exist.") }
            .addDeletionInfo()
            .savePlayerJson()

    private suspend fun Json.savePlayerJson() = jsRepository.savePlayer(this).unsafeCast<Promise<Unit>>().await()

    private fun Json.addDeletionInfo() = also {
        if (this["id"].unsafeCast<String?>() == null) {
            this["id"] = this["_id"]
        }
        this["_id"] = null
        this["timestamp"] = Date()
        this["isDeleted"] = true
        this["modifiedByUsername"] = username()
    }

    private suspend fun getPlayerJsonHistory(playerId: String) = listOf(
            playersCollection.find(json("id" to playerId)).unsafeCast<Promise<Array<Json>>>(),
            playersCollection.find(json("_id" to playerId)).unsafeCast<Promise<Array<Json>>>()
    )
            .map { it.await().toList() }
            .flatten()

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = tribeIdPlayer.player.toJson()
            .apply {
                this["id"] = tribeIdPlayer.player.id
                this["_id"] = null
                this["tribe"] = tribeIdPlayer.tribeId.value
                this["timestamp"] = Date()
                this["modifiedByUsername"] = username()
            }
            .run { jsRepository.savePlayer(this).unsafeCast<Promise<Unit>>() }
            .await()

    override fun getPlayersAsync(tribeId: TribeId) = GlobalScope.async {
        requestJsPlayers(tribeId)
                .dbJsonToPlayers()
    }

    private fun Array<Json>.dbJsonToPlayers() = map {
        it.apply {
            this["_id"] = this["id"].unsafeCast<String?>() ?: this["_id"]
        }
    }
            .groupBy { it["_id"].toString() }
            .map { it.value.latestByTimestamp() }
            .filterNotNull()
            .filter { it["isDeleted"] != true }
            .map(Json::toPlayer)

    private fun List<Json>.latestByTimestamp() = sortedByDescending { json -> json.timeStamp() }.firstOrNull()

    private fun Json.timeStamp() = this["timestamp"]?.unsafeCast<Date>()?.toDateTime()

    private suspend fun requestJsPlayers(tribeId: TribeId) = playersCollection.find(json("tribe" to tribeId.value))
            .unsafeCast<Promise<Array<Json>>>().await()
}
