package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.defaultParty
import kotlinx.serialization.Serializable

fun GqlSavePartyInput.toModel() = PartyDetails(
    id = PartyId(partyId),
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

fun GqlPartyDetails.toModel(): PartyDetails = PartyDetails(
    id = PartyId(id),
    pairingRule = PairingRule.fromValue(pairingRule),
    defaultBadgeName = defaultBadgeName,
    alternateBadgeName = alternateBadgeName,
    email = email,
    name = name,
    badgesEnabled = badgesEnabled ?: false,
    callSignsEnabled = callSignsEnabled ?: false,
    animationEnabled = animationsEnabled ?: true,
    animationSpeed = animationSpeed ?: 1.0,
)

fun GqlPartyDetails.toModelRecord(): Record<PartyDetails>? {
    return Record(
        data = PartyDetails(
            id = PartyId(id),
            pairingRule = PairingRule.fromValue(pairingRule),
            defaultBadgeName = defaultBadgeName,
            alternateBadgeName = alternateBadgeName,
            email = email,
            name = name,
            badgesEnabled = badgesEnabled ?: false,
            callSignsEnabled = callSignsEnabled ?: false,
            animationEnabled = animationsEnabled ?: true,
            animationSpeed = animationSpeed ?: 1.0,
        ),
        modifyingUserId = modifyingUserEmail ?: return null,
        isDeleted = isDeleted ?: return null,
        timestamp = timestamp ?: return null,
    )
}
