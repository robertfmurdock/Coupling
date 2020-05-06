package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetRecordSyntax
import com.zegreatrob.coupling.server.action.SuspendAction

data class TribeQuery(val tribeId: TribeId) : SuspendAction<TribeQueryDispatcher, Record<Tribe>?> {
    override suspend fun execute(dispatcher: TribeQueryDispatcher) = with(dispatcher) { perform() }
}

interface TribeQueryDispatcher : UserAuthenticatedTribeIdSyntax, TribeIdGetRecordSyntax {

    suspend fun TribeQuery.perform() = tribeId.loadRecord()

}
