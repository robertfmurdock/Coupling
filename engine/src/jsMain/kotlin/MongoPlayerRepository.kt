import com.soywiz.klock.internal.toDateTime
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.js.*

interface MongoPlayerRepository : PlayerRepository, UserContextSyntax {

    val jsRepository: dynamic

    val playersCollection: dynamic get() = jsRepository.playersCollection

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = tribeIdPlayer.toDbJson()
            .savePlayerJson()

    private fun TribeIdPlayer.toDbJson() = player.toJson()
            .apply {
                this["id"] = player.id
                this["_id"] = null
                this["tribe"] = tribeId.value
                this["timestamp"] = Date()
                this["modifiedByUsername"] = username()
            }

    private suspend fun Json.savePlayerJson() = jsRepository.savePlayer(this).unsafeCast<Promise<Unit>>().await()

    override suspend fun delete(playerId: String) = getPlayer(playerId)
            .let { it ?: throw Exception(message = "Failed to remove the player because it did not exist.") }
            .toDbJson()
            .addIsDeleted()
            .savePlayerJson()

    private suspend fun getPlayer(playerId: String) = getPlayerJsonHistory(playerId).toList()
            .latestByTimestamp()
            ?.toTribeIdPlayer()

    private suspend fun getPlayerJsonHistory(playerId: String) = listOf(
            findBy(json("id" to playerId)).unsafeCast<Promise<Array<Json>>>(),
            findBy(json("_id" to playerId)).unsafeCast<Promise<Array<Json>>>()
    )
            .map { it.await().toList() }
            .flatten()

    private fun Json.addIsDeleted() = also { this["isDeleted"] = true }

    override fun getPlayersAsync(tribeId: TribeId) = GlobalScope.async {
        requestJsPlayers(tribeId)
                .dbJsonToPlayers()
    }

    private fun Array<Json>.dbJsonToPlayers() = map { it.applyIdCorrection() }
            .groupBy { it["_id"].toString() }
            .map { it.value.latestByTimestamp() }
            .filterNotNull()
            .filter { it["isDeleted"] != true }
            .map(Json::toPlayer)

    private fun Json.applyIdCorrection() = also {
        this["_id"] = this["id"].unsafeCast<String?>() ?: this["_id"]
    }

    private fun List<Json>.latestByTimestamp() = sortedByDescending { it.timeStamp() }.firstOrNull()

    private fun Json.timeStamp() = this["timestamp"]?.unsafeCast<Date>()?.toDateTime()

    private suspend fun requestJsPlayers(tribeId: TribeId) = findBy(json("tribe" to tribeId.value))
            .unsafeCast<Promise<Array<Json>>>().await()

    private fun findBy(query: Json) = playersCollection.find(query)

    private fun Json.toTribeIdPlayer() = TribeIdPlayer(
            player = applyIdCorrection().toPlayer(),
            tribeId = TribeId(this["tribe"].unsafeCast<String>())
    )

}

