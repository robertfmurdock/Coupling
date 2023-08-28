@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import kotlinx.datetime.Instant
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
)

@Serializable
data class JsonPartyDetailsRecord(
    val id: String? = null,
    val pairingRule: Int = PairingRule.toValue(PairingRule.LongestTime),
    val badgesEnabled: Boolean = false,
    val defaultBadgeName: String = "Default",
    val alternateBadgeName: String = "Alternate",
    val email: String? = null,
    val name: String? = null,
    val callSignsEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
    val animationSpeed: Double = 1.0,
    val modifyingUserEmail: String? = null,
    val isDeleted: Boolean? = null,
    val timestamp: Instant? = null,
)

fun PartyDetails.toSerializable() = JsonPartyDetails(
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
)

fun Record<PartyDetails>.toSerializable() = JsonPartyDetailsRecord(
    id = data.id.value,
    pairingRule = PairingRule.toValue(data.pairingRule),
    badgesEnabled = data.badgesEnabled,
    defaultBadgeName = data.defaultBadgeName,
    alternateBadgeName = data.alternateBadgeName,
    email = data.email,
    name = data.name,
    callSignsEnabled = data.callSignsEnabled,
    animationsEnabled = data.animationEnabled,
    animationSpeed = data.animationSpeed,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun Record<PartyIntegration>.toSerializable() = JsonIntegrationRecord(
    slackTeam = data.slackTeam,
    slackChannel = data.slackChannel,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonIntegrationRecord.toModelRecord(): Record<PartyIntegration> = Record(
    data = PartyIntegration(
        slackTeam = slackTeam,
        slackChannel = slackChannel,
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonPartyDetails.toModel(): PartyDetails = PartyDetails(
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
)

fun JsonPartyDetailsRecord.toModelRecord(): Record<PartyDetails>? {
    return Record(
        data = PartyDetails(
            id = PartyId(id ?: return null),
            pairingRule = PairingRule.fromValue(pairingRule),
            badgesEnabled = badgesEnabled,
            defaultBadgeName = defaultBadgeName,
            alternateBadgeName = alternateBadgeName,
            email = email,
            name = name,
            callSignsEnabled = callSignsEnabled,
            animationEnabled = animationsEnabled,
            animationSpeed = animationSpeed,
        ),
        modifyingUserId = modifyingUserEmail ?: return null,
        isDeleted = isDeleted ?: return null,
        timestamp = timestamp ?: return null,
    )
}
