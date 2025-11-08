package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyId
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.uuid.Uuid

data class PairingSetId(val value: NotBlankString) {
    companion object Companion {
        @OptIn(ExperimentalKotoolsTypesApi::class)
        fun new() = PairingSetId(Uuid.random().toString().toNotBlankString().getOrThrow())
    }
}

data class PartyIdPairingSetId(val partyId: PartyId, val pairingSetId: PairingSetId)
