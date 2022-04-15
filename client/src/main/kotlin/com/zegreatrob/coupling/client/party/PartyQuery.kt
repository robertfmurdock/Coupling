package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PartyQuery(val partyId: PartyId) : SimpleSuspendAction<PartyQueryDispatcher, Party?> {
    override val performFunc = link(PartyQueryDispatcher::perform)
}

interface PartyQueryDispatcher : PartyIdGetSyntax {
    suspend fun perform(query: PartyQuery) = query.partyId.get()
}
