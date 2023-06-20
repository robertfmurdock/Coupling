package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdLoadSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PartyQuery(val partyId: PartyId) :
    SimpleSuspendAction<PartyQuery.Dispatcher, Record<PartyDetails>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : UserAuthenticatedPartyIdSyntax, PartyIdLoadSyntax {
        suspend fun perform(query: PartyQuery) = query.partyId.load()
    }
}
