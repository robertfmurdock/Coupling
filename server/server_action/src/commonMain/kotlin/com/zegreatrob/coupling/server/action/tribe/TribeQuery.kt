package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetRecordSyntax

data class TribeQuery(val tribeId: TribeId) :
    SimpleSuspendResultAction<TribeQueryDispatcher, Record<Tribe>?> {
    override val performFunc = link(TribeQueryDispatcher::perform)
}

interface TribeQueryDispatcher : UserAuthenticatedTribeIdSyntax, TribeIdGetRecordSyntax {
    suspend fun perform(query: TribeQuery) = query.tribeId.loadRecord().successResult()
}
