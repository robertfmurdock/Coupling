package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId

interface PlayerRepository : PlayerListGet,
    PlayerSave,
    PlayerDelete,
    PlayerListGetDeleted

interface PlayerEmailRepository : PlayerRepository, PlayerListGetByEmail

interface PlayerDelete {
    suspend fun deletePlayer(tribeId: TribeId, playerId: String): Boolean
}

interface PlayerSave {
    suspend fun save(tribeIdPlayer: TribeIdPlayer)
}

interface PlayerListGet {
    suspend fun getPlayers(tribeId: TribeId): List<TribeRecord<Player>>
}

interface PlayerListGetDeleted {
    suspend fun getDeleted(tribeId: TribeId): List<TribeRecord<Player>>
}

interface PlayerListGetByEmail {
    suspend fun getPlayerIdsByEmail(email: String): List<TribeElement<String>>
}