package com.zegreatrob.coupling.mongo.player

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class RetiredPlayersQuery(val tribeId: TribeId) : Action

interface RetiredPlayersQueryDispatcher : ActionLoggingSyntax, TribeIdRetiredPlayersSyntax {
    suspend fun RetiredPlayersQuery.perform() = logAsync { tribeId.loadRetiredPlayers() }
}
