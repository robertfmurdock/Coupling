@file:UseSerializers(TribeIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.PartyId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class DeletePinInput(override val tribeId: PartyId, val pinId: String) : TribeInput
