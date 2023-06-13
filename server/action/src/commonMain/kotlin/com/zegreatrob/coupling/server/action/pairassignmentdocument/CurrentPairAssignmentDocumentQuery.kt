package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class CurrentPairAssignmentDocumentQuery(val partyId: PartyId) :
    SimpleSuspendAction<CurrentPairAssignmentDocumentQuery.Dispatcher, PartyRecord<PairAssignmentDocument>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        val pairAssignmentDocumentRepository: PairAssignmentDocumentGetCurrent

        suspend fun perform(query: CurrentPairAssignmentDocumentQuery) =
            pairAssignmentDocumentRepository.getCurrentPairAssignments(query.partyId)
    }
}
