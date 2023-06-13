package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PairAssignmentDocumentListQuery(val partyId: PartyId) :
    SimpleSuspendAction<PairAssignmentDocumentListQuery.Dispatcher, List<PartyRecord<PairAssignmentDocument>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PartyIdPairAssignmentRecordsSyntax {
        suspend fun perform(query: PairAssignmentDocumentListQuery) = query.partyId.loadPairAssignmentRecords()
    }
}
