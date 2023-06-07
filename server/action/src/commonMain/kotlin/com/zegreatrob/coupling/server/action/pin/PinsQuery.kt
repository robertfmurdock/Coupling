package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPinRecordsSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object PinsQuery : SimpleSuspendAction<PinsQuery.Dispatcher, List<PartyRecord<Pin>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : CurrentPartyIdSyntax, PartyIdPinRecordsSyntax {
        suspend fun perform(query: PinsQuery) = currentPartyId.getPinRecords()
    }
}
