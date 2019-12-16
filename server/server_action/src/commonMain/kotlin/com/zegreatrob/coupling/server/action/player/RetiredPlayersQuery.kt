package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.repository.player.TribeIdRetiredPlayersSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class RetiredPlayersQuery(val tribeId: TribeId) : Action

interface RetiredPlayersQueryDispatcher : ActionLoggingSyntax,
    TribeIdRetiredPlayersSyntax {
    suspend fun RetiredPlayersQuery.perform() = logAsync { tribeId.loadRetiredPlayers() }
}
