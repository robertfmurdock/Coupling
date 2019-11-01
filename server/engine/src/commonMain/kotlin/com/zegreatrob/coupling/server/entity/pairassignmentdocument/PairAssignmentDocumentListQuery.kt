package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentGetter
import com.zegreatrob.coupling.model.tribe.TribeId

data class PairAssignmentDocumentListQuery(val tribeId: TribeId) : Action

interface PairAssignmentDocumentListQueryDispatcher : ActionLoggingSyntax, TribeIdPairAssignmentDocumentGetSyntax {

    suspend fun PairAssignmentDocumentListQuery.perform() = logAsync { tribeId.run { loadPairAssignmentDocumentList() } }

}

interface TribeIdPairAssignmentDocumentGetSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetter

    suspend fun TribeId.loadPairAssignmentDocumentList() = pairAssignmentDocumentRepository.getPairAssignmentsAsync(this).await()
}
