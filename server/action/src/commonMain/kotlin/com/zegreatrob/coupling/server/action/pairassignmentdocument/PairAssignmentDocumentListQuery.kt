package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.stats.medianSpinDuration
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairAssignmentDocumentListQuery(val partyId: PartyId) {
    interface Dispatcher : PartyIdPairAssignmentRecordsSyntax {
        suspend fun perform(query: PairAssignmentDocumentListQuery) = query.partyId.loadPairAssignmentRecords()
    }
}

@ActionMint
data class MedianSpinDurationQuery(val partyId: PartyId) {
    interface Dispatcher : PartyIdPairAssignmentRecordsSyntax {
        suspend fun perform(query: MedianSpinDurationQuery) = query.partyId.loadPairAssignmentRecords()
            .elements
            .medianSpinDuration()
    }
}
