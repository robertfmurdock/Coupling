package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

interface PlayerRepository : PlayerGetter, PlayerSaver, PlayerDeleter, PlayerGetDeleted, PlayerGetByEmail

interface PlayerDeleter {
    suspend fun delete(playerId: String)
}

interface PlayerSaver {
    suspend fun save(tribeIdPlayer: TribeIdPlayer)
}

interface PlayerGetter {
    fun getPlayersAsync(tribeId: TribeId): Deferred<List<Player>>
}

interface PlayerGetDeleted {
    fun getDeletedAsync(tribeId: TribeId): Deferred<List<Player>>
}

interface PlayerGetByEmail {
    fun getPlayersByEmailAsync(email: String): Deferred<List<TribeIdPlayer>>
}