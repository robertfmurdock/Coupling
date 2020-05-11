package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax

data class TribeQuery(val tribeId: TribeId) : SuspendAction<TribeQueryDispatcher, Tribe?> {
    override suspend fun execute(dispatcher: TribeQueryDispatcher) = with(dispatcher) { perform() }
}

interface TribeQueryDispatcher : TribeIdGetSyntax {
    suspend fun TribeQuery.perform() = tribeId.get().successResult()
}
