package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.TribeIdRetiredPlayerRecordsSyntax
import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult

data class RetiredPlayersQuery(val tribeId: TribeId) :
    SuspendAction<RetiredPlayersQueryDispatcher, List<TribeRecord<Player>>> {
    override suspend fun execute(dispatcher: RetiredPlayersQueryDispatcher) = with(dispatcher) { perform() }
}

interface RetiredPlayersQueryDispatcher : TribeIdRetiredPlayerRecordsSyntax {
    suspend fun RetiredPlayersQuery.perform() = tribeId.loadRetiredPlayerRecords().successResult()
}
