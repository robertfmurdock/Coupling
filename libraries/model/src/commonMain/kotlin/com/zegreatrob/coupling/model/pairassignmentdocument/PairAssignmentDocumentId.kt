package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyId
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.uuid.Uuid

data class PairAssignmentDocumentId(val value: NotBlankString) {
    companion object {
        @OptIn(ExperimentalKotoolsTypesApi::class)
        fun new() = PairAssignmentDocumentId(Uuid.random().toString().toNotBlankString().getOrThrow())
    }
}

data class PartyIdPairAssignmentDocumentId(val partyId: PartyId, val pairAssignmentDocumentId: PairAssignmentDocumentId)
