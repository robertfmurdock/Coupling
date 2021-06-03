package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

object PairAssignmentDocumentListQuery :
    SimpleSuspendResultAction<PairAssignmentDocumentListQueryDispatcher, List<TribeRecord<PairAssignmentDocument>>> {
    override val performFunc = link(PairAssignmentDocumentListQueryDispatcher::perform)
}

interface PairAssignmentDocumentListQueryDispatcher : TribeIdPairAssignmentRecordsSyntax, CurrentTribeIdSyntax {
    suspend fun perform(query: PairAssignmentDocumentListQuery) = currentTribeId.loadPairAssignmentRecords()
        .successResult()
}
