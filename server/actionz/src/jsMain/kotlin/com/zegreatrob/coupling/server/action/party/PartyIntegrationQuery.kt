package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyIdLoadIntegrationSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PartyIntegrationQuery(val partyId: PartyId) {
    interface Dispatcher : PartyIdLoadIntegrationSyntax {
        suspend fun perform(query: PartyIntegrationQuery) = query.partyId.loadIntegrationRecord()
    }
}
