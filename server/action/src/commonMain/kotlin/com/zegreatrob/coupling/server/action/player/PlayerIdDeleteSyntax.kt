package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.player.PlayerDeleter
import com.zegreatrob.coupling.model.player.TribeIdPlayerId

interface PlayerIdDeleteSyntax {
    val playerRepository: PlayerDeleter
    suspend fun TribeIdPlayerId.deletePlayer() = playerRepository.deletePlayer(tribeId, playerId)
}
