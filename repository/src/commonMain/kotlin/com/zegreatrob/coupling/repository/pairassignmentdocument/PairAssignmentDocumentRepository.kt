package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId

interface PairAssignmentDocumentRepository : PairAssignmentDocumentSave,
    PairAssignmentDocumentGet,
    PairAssignmentDocumentGetCurrent,
    PairAssignmentDocumentDelete

interface PairAssignmentDocumentDelete {
    suspend fun delete(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId): Boolean
}

interface PairAssignmentDocumentGet {
    suspend fun getPairAssignments(tribeId: TribeId): List<TribeRecord<PairAssignmentDocument>>
}

interface PairAssignmentDocumentGetCurrent {
    suspend fun getCurrentPairAssignments(tribeId: TribeId): TribeRecord<PairAssignmentDocument>?
}

interface PairAssignmentDocumentSave {
    suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument)
}