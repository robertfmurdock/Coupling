package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

object PairAssignmentDocumentListQuery :
    SimpleSuspendResultAction<PairAssignmentDocumentListQueryDispatcher, List<PartyRecord<PairAssignmentDocument>>> {
    override val performFunc = link(PairAssignmentDocumentListQueryDispatcher::perform)
}

interface PairAssignmentDocumentListQueryDispatcher : PartyIdPairAssignmentRecordsSyntax, CurrentTribeIdSyntax {
    suspend fun perform(query: PairAssignmentDocumentListQuery) = currentPartyId.loadPairAssignmentRecords()
        .successResult()
}
