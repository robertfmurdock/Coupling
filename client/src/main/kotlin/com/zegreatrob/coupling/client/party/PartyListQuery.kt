package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.party.PartyListSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object PartyListQuery : SimpleSuspendAction<PartyListQueryDispatcher, List<Party>?> {
    override val performFunc = link(PartyListQueryDispatcher::perform)
}

interface PartyListQueryDispatcher : PartyListSyntax {
    suspend fun perform(query: PartyListQuery) = getParties()
}
