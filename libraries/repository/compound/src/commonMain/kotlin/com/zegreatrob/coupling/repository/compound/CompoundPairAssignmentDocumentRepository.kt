package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository

class CompoundPairAssignmentDocumentRepository(
    private val repository1: PairAssignmentDocumentRepository,
    private val repository2: PairAssignmentDocumentRepository,
) : PairAssignmentDocumentRepository by repository1 {

    override suspend fun save(partyPairDocument: PartyElement<PairingSet>) = arrayOf(repository1, repository2)
        .forEach { it.save(partyPairDocument) }

    override suspend fun deleteIt(partyId: PartyId, pairingSetId: PairingSetId) = repository1.deleteIt(partyId, pairingSetId).also {
        repository2.deleteIt(partyId, pairingSetId)
    }
}
