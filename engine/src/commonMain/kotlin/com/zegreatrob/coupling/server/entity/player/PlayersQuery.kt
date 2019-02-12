package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class PlayersQuery(val tribeId: TribeId)

interface PlayersQueryDispatcher : TribeIdPlayersSyntax {
    suspend fun PlayersQuery.perform() = tribeId.loadPlayers()
}
