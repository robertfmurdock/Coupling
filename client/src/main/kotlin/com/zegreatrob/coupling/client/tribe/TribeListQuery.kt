package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeListSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object TribeListQuery : SimpleSuspendAction<TribeListQueryDispatcher, List<Tribe>?> {
    override val performFunc = link(TribeListQueryDispatcher::perform)
}

interface TribeListQueryDispatcher : TribeListSyntax {
    suspend fun perform(query: TribeListQuery) = getTribes()
}
