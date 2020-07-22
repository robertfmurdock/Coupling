package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax

data class TribeQuery(val tribeId: TribeId) : SimpleSuspendResultAction<TribeQueryDispatcher, Tribe> {
    override val performFunc = link(TribeQueryDispatcher::perform)
}

interface TribeQueryDispatcher : TribeIdGetSyntax {
    suspend fun perform(query: TribeQuery) = query.tribeId.get().asResult()

    private fun Tribe?.asResult(): Result<Tribe> = this?.successResult() ?: NotFoundResult("Tribe")
}
