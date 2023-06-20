@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import korlibs.time.DateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonPartyDetails(
    val id: String,
    val pairingRule: Int = PairingRule.toValue(PairingRule.LongestTime),
    val badgesEnabled: Boolean = false,
    val defaultBadgeName: String = "Default",
    val alternateBadgeName: String = "Alternate",
    val email: String? = null,
    val name: String? = null,
    val callSignsEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
    val animationSpeed: Double = 1.0,
    val slackTeam: String? = null,
    val slackChannel: String? = null,
)

@Serializable
data class JsonPartyDetailsRecord(
    val id: PartyId,
    val pairingRule: Int = PairingRule.toValue(PairingRule.LongestTime),
    val badgesEnabled: Boolean = false,
    val defaultBadgeName: String = "Default",
    val alternateBadgeName: String = "Alternate",
    val email: String? = null,
    val name: String? = null,
    val callSignsEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
    val slackTeam: String? = null,
    val slackChannel: String? = null,
    val animationSpeed: Double = 1.0,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: DateTime,
)

fun Party.toSerializable() = JsonPartyDetails(
    id = id.value,
    pairingRule = PairingRule.toValue(pairingRule),
    badgesEnabled = badgesEnabled,
    defaultBadgeName = defaultBadgeName,
    alternateBadgeName = alternateBadgeName,
    email = email,
    name = name,
    callSignsEnabled = callSignsEnabled,
    animationsEnabled = animationEnabled,
    animationSpeed = animationSpeed,
    slackTeam = slackTeam,
    slackChannel = slackChannel,
)

fun Record<Party>.toSerializable() = JsonPartyDetailsRecord(
    id = data.id,
    pairingRule = PairingRule.toValue(data.pairingRule),
    badgesEnabled = data.badgesEnabled,
    defaultBadgeName = data.defaultBadgeName,
    alternateBadgeName = data.alternateBadgeName,
    email = data.email,
    name = data.name,
    callSignsEnabled = data.callSignsEnabled,
    animationsEnabled = data.animationEnabled,
    animationSpeed = data.animationSpeed,
    slackTeam = data.slackTeam,
    slackChannel = data.slackChannel,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonPartyDetails.toModel(): Party = Party(
    id = PartyId(id),
    pairingRule = PairingRule.fromValue(pairingRule),
    badgesEnabled = badgesEnabled,
    defaultBadgeName = defaultBadgeName,
    alternateBadgeName = alternateBadgeName,
    email = email,
    name = name,
    callSignsEnabled = callSignsEnabled,
    animationEnabled = animationsEnabled,
    animationSpeed = animationSpeed,
    slackTeam = slackTeam,
    slackChannel = slackChannel,
)

fun JsonPartyDetailsRecord.toModelRecord(): Record<Party> = Record(
    data = Party(
        id = id,
        pairingRule = PairingRule.fromValue(pairingRule),
        badgesEnabled = badgesEnabled,
        defaultBadgeName = defaultBadgeName,
        alternateBadgeName = alternateBadgeName,
        email = email,
        name = name,
        callSignsEnabled = callSignsEnabled,
        animationEnabled = animationsEnabled,
        animationSpeed = animationSpeed,
        slackTeam = slackTeam,
        slackChannel = slackChannel,
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
