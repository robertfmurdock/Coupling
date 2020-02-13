package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerRepository

class DynamoPlayerRepository private constructor() : PlayerRepository {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPlayerRepository>, DynamoDBSyntax by DynamoDbProvider,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoItemDeleteSyntax,
        DynamoPlayerJsonMapping {
        override val construct = ::DynamoPlayerRepository
        override val tableName: String = "PLAYER"
    }

    override suspend fun getPlayers(tribeId: TribeId) = tribeId.scanForItemList().map { it.toPlayer() }

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = performPutItem(tribeIdPlayer.toDynamoJson())

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = performDelete(playerId, tribeId)

    override suspend fun getDeleted(tribeId: TribeId) = tribeId.scanForDeletedItemList().map { it.toPlayer() }

}

