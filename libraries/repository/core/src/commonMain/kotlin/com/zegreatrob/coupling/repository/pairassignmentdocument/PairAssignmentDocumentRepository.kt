package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId

interface PairAssignmentDocumentRepository :
    PairAssignmentDocumentSave,
    PairAssignmentDocumentGet,
    PairAssignmentDocumentGetCurrent,
    PairAssignmentDocumentDelete

interface PairAssignmentDocumentDelete {
    suspend fun deleteIt(partyId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId): Boolean
}

interface PairAssignmentDocumentGet {
    suspend fun loadPairAssignments(partyId: PartyId): List<PartyRecord<PairAssignmentDocument>>
}

interface PairAssignmentDocumentGetCurrent {
    suspend fun getCurrentPairAssignments(partyId: PartyId): PartyRecord<PairAssignmentDocument>?
}

interface PairAssignmentDocumentSave {
    suspend fun save(partyPairDocument: PartyElement<PairAssignmentDocument>)
}
