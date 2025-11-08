package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PartyIdPairingSetId

interface PairingSetIdDeleteSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentDelete

    suspend fun PartyIdPairingSetId.deleteIt() = pairAssignmentDocumentRepository.deleteIt(
        partyId,
        pairingSetId,
    )
}
