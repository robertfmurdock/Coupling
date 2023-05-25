package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

object CurrentPairAssignmentDocumentQuery :
    SimpleSuspendResultAction<CurrentPairAssignmentDocumentQueryDispatcher, PartyRecord<PairAssignmentDocument>> {
    override val performFunc = link(CurrentPairAssignmentDocumentQueryDispatcher::perform)
}

interface CurrentPairAssignmentDocumentQueryDispatcher : CurrentPartyIdSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetCurrent

    suspend fun perform(query: CurrentPairAssignmentDocumentQuery) =
        pairAssignmentDocumentRepository.getCurrentPairAssignments(currentPartyId)
            ?.successResult()
            ?: NotFoundResult("currentPairAssignment")
}
