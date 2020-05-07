package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax
import com.zegreatrob.coupling.server.action.SuspendAction
import com.zegreatrob.coupling.server.action.successResult

object PairAssignmentDocumentListQuery :
    SuspendAction<PairAssignmentDocumentListQueryDispatcher, List<TribeRecord<PairAssignmentDocument>>> {
    override suspend fun execute(dispatcher: PairAssignmentDocumentListQueryDispatcher) = with(dispatcher) { perform() }
}

interface PairAssignmentDocumentListQueryDispatcher : TribeIdPairAssignmentRecordsSyntax, CurrentTribeIdSyntax {
    suspend fun PairAssignmentDocumentListQuery.perform() = currentTribeId.loadPairAssignmentRecords().successResult()
}
