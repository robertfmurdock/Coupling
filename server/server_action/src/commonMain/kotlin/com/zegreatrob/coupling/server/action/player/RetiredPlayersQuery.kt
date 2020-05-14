package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.TribeIdRetiredPlayerRecordsSyntax

data class RetiredPlayersQuery(val tribeId: TribeId) :
    SimpleSuspendAction<RetiredPlayersQueryDispatcher, List<TribeRecord<Player>>> {
    override val performFunc = link(RetiredPlayersQueryDispatcher::perform)
}

interface RetiredPlayersQueryDispatcher : TribeIdRetiredPlayerRecordsSyntax {
    suspend fun perform(query: RetiredPlayersQuery) = query.tribeId.loadRetiredPlayerRecords().successResult()
}
