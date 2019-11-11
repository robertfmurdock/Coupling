package com.zegreatrob.coupling.mongo.player

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerRepository
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.with
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.DbRecordDeleteSyntax
import com.zegreatrob.coupling.mongo.DbRecordLoadSyntax
import com.zegreatrob.coupling.mongo.DbRecordSaveSyntax
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface MongoPlayerRepository : PlayerRepository,
    PlayerToDbSyntax,
    DbRecordSaveSyntax,
    DbRecordLoadSyntax,
    DbRecordDeleteSyntax {

    val jsRepository: dynamic
    val playersCollection: dynamic get() = jsRepository.playersCollection

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = tribeIdPlayer.toDbJson()
        .savePlayerJson()

    private fun TribeIdPlayer.toDbJson() = player.toDbJson()
        .apply { this["tribe"] = tribeId.value }

    private suspend fun Json.savePlayerJson() = this.save(playersCollection)

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = deleteEntity(
        playerId,
        playersCollection,
        "Player",
        { toTribeIdPlayer() },
        { toDbJson() }
    )

    override suspend fun getPlayers(tribeId: TribeId): List<Player> =
        findByQuery(json("tribe" to tribeId.value), playersCollection)
            .map { it.fromDbToPlayer() }

    override fun getPlayersByEmailAsync(email: String) = GlobalScope.async {
        getLatestRecordsRelatedToAsync(json("email" to email), playersCollection).await()
            .map { it.fromDbToPlayer() with TribeId(it["tribe"].toString()) }
            .filter { it.player.email == email }
    }

    private fun CoroutineScope.getLatestRecordsRelatedToAsync(query: Json, collection: dynamic) = async {
        rawFindBy(query, collection).await()
            .map { it.applyIdCorrection() }
            .map { it["_id"].toString() }
            .distinct()
            .map {
                async {
                    getLatestRecordWithId(it, collection)
                }
            }
            .mapNotNull { it.await() }
    }

    override suspend fun getDeleted(tribeId: TribeId): List<Player> = findDeletedByQuery(tribeId, playersCollection)
        .map { it.fromDbToPlayer() }

    private fun Json.toTribeIdPlayer() = TribeIdPlayer(
        tribeId = TribeId(this["tribe"].unsafeCast<String>()),
        player = applyIdCorrection().fromDbToPlayer()
    )

}