package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax

object PairAssignmentDocumentListQuery :
    SimpleSuspendAction<PairAssignmentDocumentListQueryDispatcher, List<TribeRecord<PairAssignmentDocument>>> {
    override val perform = link(PairAssignmentDocumentListQueryDispatcher::perform)
}

interface PairAssignmentDocumentListQueryDispatcher : TribeIdPairAssignmentRecordsSyntax, CurrentTribeIdSyntax {
    suspend fun perform(query: PairAssignmentDocumentListQuery) = currentTribeId.loadPairAssignmentRecords()
        .successResult()
}
