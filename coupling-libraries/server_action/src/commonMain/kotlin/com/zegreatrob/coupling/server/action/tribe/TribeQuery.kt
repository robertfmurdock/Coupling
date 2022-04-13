package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetRecordSyntax

data class TribeQuery(val tribeId: PartyId) :
    SimpleSuspendResultAction<PartyQueryDispatcher, Record<Party>> {
    override val performFunc = link(PartyQueryDispatcher::perform)
}

interface PartyQueryDispatcher : UserAuthenticatedPartyIdSyntax, TribeIdGetRecordSyntax {
    suspend fun perform(query: TribeQuery) = query.tribeId.loadRecord()?.let { SuccessfulResult(it) }
        ?: NotFoundResult("tribe")
}
