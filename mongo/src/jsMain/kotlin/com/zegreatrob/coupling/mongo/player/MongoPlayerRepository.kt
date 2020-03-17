package com.zegreatrob.coupling.mongo.player

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.player.tribeId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.mongo.DbRecordDeleteSyntax
import com.zegreatrob.coupling.mongo.DbRecordLoadSyntax
import com.zegreatrob.coupling.mongo.DbRecordSaveSyntax
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.player.PlayerRepository
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface MongoPlayerRepository : PlayerRepository,
    PlayerListGetByEmail,
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

    private suspend fun Json.savePlayerJson() = save(playersCollection)

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = deleteEntity(
        playerId,
        playersCollection,
        "Player",
        { toTribeIdPlayer() },
        { toDbJson() }
    )

    override suspend fun getPlayers(tribeId: TribeId) = getPlayerRecords(tribeId).groupBy { it.data.element.id }
        .map { group -> group.value.asSequence().sortedByDescending { record -> record.timestamp }.first() }
        .filterNot { it.isDeleted }

    suspend fun getPlayerRecords(tribeId: TribeId) = rawFindBy(json("tribe" to tribeId.value), playersCollection)
        .await()
        .map { it.toPlayerRecord() }

    override suspend fun getPlayerIdsByEmail(email: String) =
        getLatestRecordsRelatedToAsync(json("email" to email), playersCollection)
            .map { it.toPlayerRecord().data }
            .filter { it.player.email == email }
            .map { it.id.with(it.player.id!!) }

    private suspend fun getLatestRecordsRelatedToAsync(query: Json, collection: dynamic) =
        rawFindBy(query, collection).await()
            .map { it.applyIdCorrection() }
            .map { it["_id"].toString() }
            .distinct()
            .mapNotNull { getLatestRecordWithId(it, collection) }

    override suspend fun getDeleted(tribeId: TribeId): List<Record<TribeIdPlayer>> =
        findDeletedByQuery(tribeId, playersCollection)
            .map { it.toPlayerRecord() }

    private fun Json.toTribeIdPlayer() = TribeId(this["tribe"].unsafeCast<String>()).with(
        element = applyIdCorrection().toPlayerRecord().data.player
    )

}