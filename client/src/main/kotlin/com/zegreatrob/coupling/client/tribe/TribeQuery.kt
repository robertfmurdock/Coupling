package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class TribeQuery(val tribeId: PartyId) : SimpleSuspendAction<TribeQueryDispatcher, Party?> {
    override val performFunc = link(TribeQueryDispatcher::perform)
}

interface TribeQueryDispatcher : TribeIdGetSyntax {
    suspend fun perform(query: TribeQuery) = query.tribeId.get()
}
