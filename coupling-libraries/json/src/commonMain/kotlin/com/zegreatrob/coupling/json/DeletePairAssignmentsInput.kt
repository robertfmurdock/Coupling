@file:UseSerializers(PartyIdSerializer::class, PairAssignmentDocumentIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class DeletePairAssignmentsInput(val pairAssignmentsId: PairAssignmentDocumentId, override val partyId: PartyId) :
    PartyInput
