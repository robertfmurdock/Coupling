package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax

data class TribeQuery(val tribeId: TribeId) : SimpleSuspendAction<TribeQueryDispatcher, Tribe?> {
    override val performFunc = link(TribeQueryDispatcher::perform)
}

interface TribeQueryDispatcher : TribeIdGetSyntax {
    suspend fun perform(query: TribeQuery) = query.tribeId.get().successResult()
}
