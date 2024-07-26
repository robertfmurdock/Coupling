@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
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

fun Record<PartyDetails>.toSerializable() = GqlPartyDetails(
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

fun Record<PartyIntegration>.toSerializable() = GqlPartyIntegration(
    slackTeam = data.slackTeam,
    slackChannel = data.slackChannel,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun GqlPartyIntegration.toModelRecord(): Record<PartyIntegration> = Record(
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

fun GqlPartyDetails.toModelRecord(): Record<PartyDetails>? {
    return Record(
        data = PartyDetails(
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
        ),
        modifyingUserId = modifyingUserEmail ?: return null,
        isDeleted = isDeleted ?: return null,
        timestamp = timestamp ?: return null,
    )
}
