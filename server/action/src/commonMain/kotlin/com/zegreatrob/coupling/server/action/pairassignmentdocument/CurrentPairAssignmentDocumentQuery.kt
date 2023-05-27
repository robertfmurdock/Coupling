package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

object CurrentPairAssignmentDocumentQuery :
    SimpleSuspendResultAction<CurrentPairAssignmentDocumentQuery.Dispatcher, PartyRecord<PairAssignmentDocument>> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : CurrentPartyIdSyntax {
        val pairAssignmentDocumentRepository: PairAssignmentDocumentGetCurrent

        suspend fun perform(query: CurrentPairAssignmentDocumentQuery) =
            pairAssignmentDocumentRepository.getCurrentPairAssignments(currentPartyId)
                ?.successResult()
                ?: NotFoundResult("currentPairAssignment")
    }
}
