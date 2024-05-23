package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.player.PartyRetiredPlayerRecordsSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class RetiredPlayersQuery(val partyId: PartyId) {
    interface Dispatcher : PartyRetiredPlayerRecordsSyntax {
        suspend fun perform(query: RetiredPlayersQuery) = query.partyId.loadRetiredPlayerRecords()
    }
}
