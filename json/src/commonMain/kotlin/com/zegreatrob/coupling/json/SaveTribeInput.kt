@file:UseSerializers(TribeIdSerializer::class)
package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class SaveTribeInput(
    val tribeId: TribeId,
    val name: String?,
    val email: String?,
    val pairingRule: Int?,
    val badgesEnabled: Boolean?,
    val defaultBadgeName: String?,
    val alternateBadgeName: String?,
    val callSignsEnabled: Boolean?,
    val animationsEnabled: Boolean?,
    val animationSpeed: Double?,
)