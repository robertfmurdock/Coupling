package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax

object PairAssignmentDocumentListQuery :
    SimpleSuspendAction<PairAssignmentDocumentListQueryDispatcher, List<TribeRecord<PairAssignmentDocument>>> {
    override val performFunc = link(PairAssignmentDocumentListQueryDispatcher::perform)
}

interface PairAssignmentDocumentListQueryDispatcher : TribeIdPairAssignmentRecordsSyntax, CurrentTribeIdSyntax {
    suspend fun perform(query: PairAssignmentDocumentListQuery) = currentTribeId.loadPairAssignmentRecords()
        .successResult()
}
