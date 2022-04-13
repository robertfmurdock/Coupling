package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.party.PartyListSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object TribeListQuery : SimpleSuspendAction<PartyListQueryDispatcher, List<Party>?> {
    override val performFunc = link(PartyListQueryDispatcher::perform)
}

interface PartyListQueryDispatcher : PartyListSyntax {
    suspend fun perform(query: TribeListQuery) = getParties()
}
