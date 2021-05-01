package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class TribeQuery(val tribeId: TribeId) : SimpleSuspendAction<TribeQueryDispatcher, Tribe?> {
    override val performFunc = link(TribeQueryDispatcher::perform)
}

interface TribeQueryDispatcher : TribeIdGetSyntax {
    suspend fun perform(query: TribeQuery) = query.tribeId.get()
}
