package com.zegreatrob.coupling.json

import kotlinx.serialization.Serializable

@Serializable
data class SaveTribeInput(
    val tribeId: String,
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