package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.server.action.AuthorizedTribeIdSyntax

object PairAssignmentDocumentListQuery : Action

interface PairAssignmentDocumentListQueryDispatcher : ActionLoggingSyntax, TribeIdPairAssignmentDocumentGetSyntax,
    AuthorizedTribeIdSyntax {
    suspend fun PairAssignmentDocumentListQuery.perform() =
        logAsync { authorizedTribeId?.loadPairAssignmentDocumentList() ?: emptyList() }
}

interface TribeIdPairAssignmentDocumentGetSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGet
    suspend fun TribeId.loadPairAssignmentDocumentList() = pairAssignmentDocumentRepository.getPairAssignments(this)
}
