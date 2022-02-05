@file:UseSerializers(TribeIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class DeletePairAssignmentsInput(val pairAssignmentsId: String, override val tribeId: TribeId) : TribeInput
