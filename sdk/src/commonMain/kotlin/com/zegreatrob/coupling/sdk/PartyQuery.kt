package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PartyQuery(val partyId: PartyId) : SimpleSuspendAction<PartyQuery.Dispatcher, Party?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: PartyQuery): Party?
    }
}

interface ClientPartyQueryDispatcher : PartyIdGetSyntax, PartyQuery.Dispatcher {
    override suspend fun perform(query: PartyQuery) = query.partyId.get()
}
