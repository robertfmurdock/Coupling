package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class PairAssignmentDocumentListQuery(val partyId: PartyId) {
    interface Dispatcher : PartyIdPairAssignmentRecordsSyntax {
        suspend fun perform(query: PairAssignmentDocumentListQuery) = query.partyId.loadPairAssignmentRecords()
    }
}
