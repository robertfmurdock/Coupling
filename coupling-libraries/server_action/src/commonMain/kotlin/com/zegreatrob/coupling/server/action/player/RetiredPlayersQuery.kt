package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.TribeIdRetiredPlayerRecordsSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

object RetiredPlayersQuery : SimpleSuspendResultAction<RetiredPlayersQueryDispatcher, List<TribeRecord<Player>>> {
    override val performFunc = link(RetiredPlayersQueryDispatcher::perform)
}

interface RetiredPlayersQueryDispatcher : CurrentTribeIdSyntax, TribeIdRetiredPlayerRecordsSyntax {
    suspend fun perform(query: RetiredPlayersQuery) = currentTribeId.loadRetiredPlayerRecords().successResult()
}
