package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.actionFunc.SimpleSuspendResultAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeListSyntax

object TribeListQuery :
    SimpleSuspendResultAction<TribeListQueryDispatcher, List<Tribe>> {
    override val performFunc = link(TribeListQueryDispatcher::perform)
}

interface TribeListQueryDispatcher : TribeListSyntax {
    suspend fun perform(query: TribeListQuery) = getTribes().successResult()
}
