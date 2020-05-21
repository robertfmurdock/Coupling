package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetRecordSyntax

data class TribeQuery(val tribeId: TribeId) :
    SimpleSuspendAction<TribeQueryDispatcher, Record<Tribe>?> {
    override val performFunc = link(TribeQueryDispatcher::perform)
}

interface TribeQueryDispatcher : UserAuthenticatedTribeIdSyntax, TribeIdGetRecordSyntax {
    suspend fun perform(query: TribeQuery) = query.tribeId.loadRecord().successResult()
}
