package com.zegreatrob.coupling.mongo.player

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.core.entity.tribe.TribeId

data class RetiredPlayersQuery(val tribeId: TribeId) : Action

interface RetiredPlayersQueryDispatcher : ActionLoggingSyntax, TribeIdRetiredPlayersSyntax {
    suspend fun RetiredPlayersQuery.perform() = logAsync { tribeId.loadRetiredPlayers() }
}
