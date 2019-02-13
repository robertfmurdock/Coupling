package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class PlayersQuery(val tribeId: TribeId) : Action

interface PlayersQueryDispatcher : ActionLoggingSyntax, TribeIdPlayersSyntax {
    suspend fun PlayersQuery.perform() = logAsync { tribeId.loadPlayers() }
}
