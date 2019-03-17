package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.player.with
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.server.DbRecordDeleteSyntax
import com.zegreatrob.coupling.server.DbRecordLoadSyntax
import com.zegreatrob.coupling.server.DbRecordSaveSyntax
import com.zegreatrob.coupling.server.PlayerToDbSyntax
import kotlinx.coroutines.*
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

    override suspend fun delete(playerId: String) = deleteEntity(
            playerId,
            playersCollection,
            "Player",
            { toTribeIdPlayer() },
            { toDbJson() }
    )

    override fun getPlayersAsync(tribeId: TribeId) = GlobalScope.async {
        findByQuery(json("tribe" to tribeId.value), playersCollection)
                .map { it.fromDbToPlayer() }
    }

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

    override fun getDeletedAsync(tribeId: TribeId): Deferred<List<Player>> = GlobalScope.async {
        findDeletedByQuery(tribeId, playersCollection)
                .map { it.fromDbToPlayer() }
    }

    private fun Json.toTribeIdPlayer() = TribeIdPlayer(
            tribeId = TribeId(this["tribe"].unsafeCast<String>()),
            player = applyIdCorrection().fromDbToPlayer()
    )

}