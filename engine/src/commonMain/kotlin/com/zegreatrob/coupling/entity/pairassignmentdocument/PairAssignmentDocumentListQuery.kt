package com.zegreatrob.coupling.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class PairAssignmentDocumentListQuery(val tribeId: TribeId)

interface PairAssignmentDocumentListQueryDispatcher : TribeIdPairAssignmentDocumentGetSyntax {

    suspend fun PairAssignmentDocumentListQuery.perform() = tribeId.run { loadPairAssignmentDocumentList() }

}

interface TribeIdPairAssignmentDocumentGetSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetter

    suspend fun TribeId.loadPairAssignmentDocumentList() = pairAssignmentDocumentRepository.getPairAssignmentsAsync(this).await()
}
