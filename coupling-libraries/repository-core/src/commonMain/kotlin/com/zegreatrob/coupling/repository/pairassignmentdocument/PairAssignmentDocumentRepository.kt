package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.PartyId

interface PairAssignmentDocumentRepository : PairAssignmentDocumentSave,
    PairAssignmentDocumentGet,
    PairAssignmentDocumentGetCurrent,
    PairAssignmentDocumentDelete

interface PairAssignmentDocumentDelete {
    suspend fun delete(tribeId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId): Boolean
}

interface PairAssignmentDocumentGet {
    suspend fun getPairAssignments(tribeId: PartyId): List<TribeRecord<PairAssignmentDocument>>
}

interface PairAssignmentDocumentGetCurrent {
    suspend fun getCurrentPairAssignments(tribeId: PartyId): TribeRecord<PairAssignmentDocument>?
}

interface PairAssignmentDocumentSave {
    suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument)
}