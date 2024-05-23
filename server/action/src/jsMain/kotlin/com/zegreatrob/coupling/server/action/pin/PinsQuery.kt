package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPinRecordsSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PinsQuery(val partyId: PartyId) {
    interface Dispatcher : PartyIdPinRecordsSyntax {
        suspend fun perform(query: PinsQuery) = query.partyId.loadPins()
    }
}
