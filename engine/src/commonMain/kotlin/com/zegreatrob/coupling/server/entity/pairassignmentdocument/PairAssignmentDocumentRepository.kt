package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

interface PairAssignmentDocumentRepository : PairAssignmentDocumentSaver, PairAssignmentDocumentGetter, PairAssignmentDocumentDeleter

interface PairAssignmentDocumentDeleter {
    suspend fun delete(pairAssignmentDocumentId: PairAssignmentDocumentId)
}

interface PairAssignmentDocumentGetter {
    fun getPairAssignmentsAsync(tribeId: TribeId): Deferred<List<PairAssignmentDocument>>
}

interface PairAssignmentDocumentSaver {
    suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument)
}