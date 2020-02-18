package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.TribeIdRetiredPlayerRecordsSyntax

data class RetiredPlayersQuery(val tribeId: TribeId) : Action

interface RetiredPlayersQueryDispatcher : ActionLoggingSyntax, TribeIdRetiredPlayerRecordsSyntax {
    suspend fun RetiredPlayersQuery.perform() = logAsync { tribeId.loadRetiredPlayerRecords() }
}
