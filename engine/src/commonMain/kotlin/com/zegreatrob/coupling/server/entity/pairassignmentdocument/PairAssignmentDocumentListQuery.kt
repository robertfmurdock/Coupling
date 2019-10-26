package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.core.entity.tribe.TribeId

data class PairAssignmentDocumentListQuery(val tribeId: TribeId) : Action

interface PairAssignmentDocumentListQueryDispatcher : ActionLoggingSyntax, TribeIdPairAssignmentDocumentGetSyntax {

    suspend fun PairAssignmentDocumentListQuery.perform() = logAsync { tribeId.run { loadPairAssignmentDocumentList() } }

}

interface TribeIdPairAssignmentDocumentGetSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetter

    suspend fun TribeId.loadPairAssignmentDocumentList() = pairAssignmentDocumentRepository.getPairAssignmentsAsync(this).await()
}
