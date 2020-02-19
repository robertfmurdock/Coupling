package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.server.action.AuthorizedTribeIdSyntax

object PairAssignmentDocumentListQuery : Action

interface PairAssignmentDocumentListQueryDispatcher : ActionLoggingSyntax, TribeIdPairAssignmentRecordsSyntax,
    AuthorizedTribeIdSyntax {
    suspend fun PairAssignmentDocumentListQuery.perform() =
        logAsync { authorizedTribeId?.loadPairAssignmentRecords() ?: emptyList() }
}
