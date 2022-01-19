package com.zegreatrob.coupling.model.tribe

data class Tribe(
    val id: TribeId,
    val pairingRule: PairingRule = PairingRule.LongestTime,
    val badgesEnabled: Boolean = false,
    val defaultBadgeName: String = "Default",
    val alternateBadgeName: String = "Alternate",
    val email: String? = null,
    val name: String? = null,
    val callSignsEnabled: Boolean = false,
    val animationEnabled: Boolean = true,
    val animationSpeed: Double = 1.0,
    val imageURL: String? = null,
)

val defaultTribe = Tribe(TribeId("DEFAULT"))
