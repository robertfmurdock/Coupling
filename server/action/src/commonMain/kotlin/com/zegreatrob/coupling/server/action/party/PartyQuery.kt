package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdGetRecordSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PartyQuery(val partyId: PartyId) :
    SimpleSuspendAction<PartyQuery.Dispatcher, Record<Party>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : UserAuthenticatedPartyIdSyntax, PartyIdGetRecordSyntax {
        suspend fun perform(query: PartyQuery) = query.partyId.loadRecord()
    }
}
