package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository

class CompoundPairAssignmentDocumentRepository(
    private val repository1: PairAssignmentDocumentRepository,
    private val repository2: PairAssignmentDocumentRepository,
) : PairAssignmentDocumentRepository by repository1 {

    override suspend fun save(partyPairDocument: PartyElement<PairAssignmentDocument>) = arrayOf(repository1, repository2)
        .forEach { it.save(partyPairDocument) }

    override suspend fun deleteIt(partyId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId) = repository1.deleteIt(partyId, pairAssignmentDocumentId).also {
        repository2.deleteIt(partyId, pairAssignmentDocumentId)
    }
}
