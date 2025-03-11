package com.zegreatrob.coupling.model.party

import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

data class PartyDetails(
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
)

data class PartyIntegration(
    val slackTeam: String?,
    val slackChannel: String?,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
val defaultParty = PartyDetails(PartyId(NotBlankString.create("DEFAULT")))
