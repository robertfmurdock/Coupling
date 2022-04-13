package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyElement

interface PartyIdPairAssignmentDocumentSaveSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentSave
    suspend fun PartyElement<PairAssignmentDocument>.save() =
        pairAssignmentDocumentRepository.save(this)
}
