package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId

interface PairAssignmentDocumentRepository : PairAssignmentDocumentSaver,
    PairAssignmentDocumentGetter,
    PairAssignmentDocumentDeleter

interface PairAssignmentDocumentDeleter {
    suspend fun delete(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId): Boolean
}

interface PairAssignmentDocumentGetter {
    suspend fun getPairAssignments(tribeId: TribeId): List<PairAssignmentDocument>
}

interface PairAssignmentDocumentSaver {
    suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument)
}