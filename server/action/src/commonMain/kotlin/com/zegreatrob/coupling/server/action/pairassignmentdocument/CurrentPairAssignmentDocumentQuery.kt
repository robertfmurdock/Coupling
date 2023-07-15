package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class CurrentPairAssignmentDocumentQuery(val partyId: PartyId) {
    interface Dispatcher {
        val pairAssignmentDocumentRepository: PairAssignmentDocumentGetCurrent

        suspend fun perform(query: CurrentPairAssignmentDocumentQuery) =

            pairAssignmentDocumentRepository.getCurrentPairAssignments(query.partyId)
    }
}
