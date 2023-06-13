package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPinRecordsSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PinsQuery(val partyId: PartyId) : SimpleSuspendAction<PinsQuery.Dispatcher, List<PartyRecord<Pin>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PartyIdPinRecordsSyntax {
        suspend fun perform(query: PinsQuery) = query.partyId.loadPins()
    }
}
