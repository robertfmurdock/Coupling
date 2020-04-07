package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax

object PairAssignmentDocumentListQuery : Action

interface PairAssignmentDocumentListQueryDispatcher : ActionLoggingSyntax, TribeIdPairAssignmentRecordsSyntax,
    CurrentTribeIdSyntax {
    suspend fun PairAssignmentDocumentListQuery.perform() =
        logAsync { currentTribeId.loadPairAssignmentRecords() }
}
