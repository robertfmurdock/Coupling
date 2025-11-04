package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.defaultParty
import kotlinx.serialization.Serializable
import org.kotools.types.ExperimentalKotoolsTypesApi

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlSavePartyInput.toModel() = PartyDetails(
    id = partyId,
    pairingRule = PairingRule.fromValue(pairingRule),
    badgesEnabled = badgesEnabled ?: defaultParty.badgesEnabled,
    defaultBadgeName = defaultBadgeName ?: defaultParty.defaultBadgeName,
    alternateBadgeName = alternateBadgeName ?: defaultParty.alternateBadgeName,
    email = email,
    name = name,
    callSignsEnabled = callSignsEnabled ?: defaultParty.callSignsEnabled,
    animationEnabled = animationsEnabled ?: defaultParty.animationEnabled,
    animationSpeed = animationSpeed ?: defaultParty.animationSpeed,
)

@Serializable
data class JsonPartyDetails(
    val id: PartyIdString,
    val pairingRule: Int = PairingRule.toValue(PairingRule.LongestTime),
    val badgesEnabled: Boolean = false,
    val defaultBadgeName: String = "Default",
    val alternateBadgeName: String = "Alternate",
    val email: String? = null,
    val name: String? = null,
    val callSignsEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
    val animationSpeed: Double = 1.0,
)

fun PartyDetails.toSerializable() = JsonPartyDetails(
    id = id,
    pairingRule = PairingRule.toValue(pairingRule),
    badgesEnabled = badgesEnabled,
    defaultBadgeName = defaultBadgeName,
    alternateBadgeName = alternateBadgeName,
    email = email,
    name = name,
    callSignsEnabled = callSignsEnabled,
    animationsEnabled = animationEnabled,
    animationSpeed = animationSpeed,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun JsonPartyDetails.toModel(): PartyDetails = PartyDetails(
    id = id,
    pairingRule = PairingRule.fromValue(pairingRule),
    defaultBadgeName = defaultBadgeName,
    alternateBadgeName = alternateBadgeName,
    email = email,
    name = name,
    badgesEnabled = badgesEnabled,
    callSignsEnabled = callSignsEnabled,
    animationEnabled = animationsEnabled,
    animationSpeed = animationSpeed,
)
