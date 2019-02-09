package com.zegreatrob.coupling.entity.player

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.server.DbRecordDeleteSyntax
import com.zegreatrob.coupling.server.DbRecordInfoSyntax
import com.zegreatrob.coupling.server.DbRecordLoadSyntax
import com.zegreatrob.coupling.server.PlayerToDbSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

interface MongoPlayerRepository : PlayerRepository,
        PlayerToDbSyntax,
        DbRecordInfoSyntax,
        DbRecordLoadSyntax,
        DbRecordDeleteSyntax {

    val jsRepository: dynamic
    val playersCollection: dynamic get() = jsRepository.playersCollection

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = tribeIdPlayer.toDbJson()
            .savePlayerJson()

    private fun TribeIdPlayer.toDbJson() = player.toDbJson()
            .apply { this["tribe"] = tribeId.value }
            .addRecordInfo()


    private suspend fun Json.savePlayerJson() = playersCollection.insert(this).unsafeCast<Promise<Unit>>().await()

    override suspend fun delete(playerId: String) =
            deleteEntity(playerId, playersCollection, "Player", { toTribeIdPlayer() }, { toDbJson() })

    override fun getPlayersAsync(tribeId: TribeId) = GlobalScope.async {
        findByQuery(json("tribe" to tribeId.value), playersCollection)
                .map { it.fromDbToPlayer() }
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