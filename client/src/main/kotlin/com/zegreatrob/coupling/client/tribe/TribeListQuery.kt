package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeListSyntax

object TribeListQuery : SuspendAction<TribeListQueryDispatcher, List<Tribe>> {
    override suspend fun execute(dispatcher: TribeListQueryDispatcher) = with(dispatcher) { perform() }
}

interface TribeListQueryDispatcher : TribeListSyntax {
    suspend fun TribeListQuery.perform() = getTribes().successResult()
}
