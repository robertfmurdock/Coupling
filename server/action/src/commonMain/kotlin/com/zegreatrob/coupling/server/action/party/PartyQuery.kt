package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdLoadSyntax
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class PartyQuery(val partyId: PartyId) {
    interface Dispatcher : UserAuthenticatedPartyIdSyntax, PartyIdLoadSyntax {
        suspend fun perform(query: PartyQuery) = query.partyId.load()
    }
}
