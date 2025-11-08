package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId

interface PairAssignmentDocumentRepository :
    PairAssignmentDocumentSave,
    PairAssignmentDocumentGet,
    PairAssignmentDocumentGetCurrent,
    PairAssignmentDocumentDelete

interface PairAssignmentDocumentDelete {
    suspend fun deleteIt(partyId: PartyId, pairingSetId: PairingSetId): Boolean
}

fun interface PairAssignmentDocumentGet {
    suspend fun loadPairAssignments(partyId: PartyId): List<PartyRecord<PairingSet>>
}

interface PairAssignmentDocumentGetCurrent {
    suspend fun getCurrentPairAssignments(partyId: PartyId): PartyRecord<PairingSet>?
}

interface PairAssignmentDocumentSave {
    suspend fun save(partyPairDocument: PartyElement<PairingSet>)
}
