@file:UseSerializers(TribeIdSerializer::class, PairAssignmentDocumentIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class DeletePairAssignmentsInput(val pairAssignmentsId: PairAssignmentDocumentId, override val tribeId: TribeId) :
    TribeInput
