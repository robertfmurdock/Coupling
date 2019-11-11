package com.zegreatrob.coupling.model.player

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.Deferred

interface PlayerRepository : PlayerGetter, PlayerSaver, PlayerDeleter, PlayerGetDeleted, PlayerGetByEmail

interface PlayerDeleter {
    suspend fun deletePlayer(tribeId: TribeId, playerId: String): Boolean
}

interface PlayerSaver {
    suspend fun save(tribeIdPlayer: TribeIdPlayer)
}

interface PlayerGetter {
    suspend fun getPlayers(tribeId: TribeId): List<Player>
}

interface PlayerGetDeleted {
    fun getDeletedAsync(tribeId: TribeId): Deferred<List<Player>>
}

interface PlayerGetByEmail {
    fun getPlayersByEmailAsync(email: String): Deferred<List<TribeIdPlayer>>
}