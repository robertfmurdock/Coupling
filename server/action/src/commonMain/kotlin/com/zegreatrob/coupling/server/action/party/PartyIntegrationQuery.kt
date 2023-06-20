package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.repository.party.PartyIdLoadIntegrationSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PartyIntegrationQuery(val partyId: PartyId) :
    SimpleSuspendAction<PartyIntegrationQuery.Dispatcher, Record<PartyIntegration>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PartyIdLoadIntegrationSyntax {
        suspend fun perform(query: PartyIntegrationQuery) = query.partyId.loadIntegrationRecord()
    }
}
