package com.zegreatrob.coupling.model.party

data class Party(
    val id: PartyId,
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
    val slackChannel: String? = null,
)

val defaultParty = Party(PartyId("DEFAULT"))
