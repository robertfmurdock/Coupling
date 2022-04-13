package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdGetRecordSyntax

data class PartyQuery(val partyId: PartyId) :
    SimpleSuspendResultAction<PartyQueryDispatcher, Record<Party>> {
    override val performFunc = link(PartyQueryDispatcher::perform)
}

interface PartyQueryDispatcher : UserAuthenticatedPartyIdSyntax, PartyIdGetRecordSyntax {
    suspend fun perform(query: PartyQuery) = query.partyId.loadRecord()?.let { SuccessfulResult(it) }
        ?: NotFoundResult("party")
}
