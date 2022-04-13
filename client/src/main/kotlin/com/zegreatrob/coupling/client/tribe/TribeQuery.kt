package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class TribeQuery(val tribeId: PartyId) : SimpleSuspendAction<PartyQueryDispatcher, Party?> {
    override val performFunc = link(PartyQueryDispatcher::perform)
}

interface PartyQueryDispatcher : PartyIdGetSyntax {
    suspend fun perform(query: TribeQuery) = query.tribeId.get()
}
