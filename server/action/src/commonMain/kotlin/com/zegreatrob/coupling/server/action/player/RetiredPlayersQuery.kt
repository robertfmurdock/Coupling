package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PartyRetiredPlayerRecordsSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class RetiredPlayersQuery(val partyId: PartyId) : SimpleSuspendAction<RetiredPlayersQuery.Dispatcher, List<PartyRecord<Player>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PartyRetiredPlayerRecordsSyntax {
        suspend fun perform(query: RetiredPlayersQuery) = query.partyId.loadRetiredPlayerRecords()
    }
}
