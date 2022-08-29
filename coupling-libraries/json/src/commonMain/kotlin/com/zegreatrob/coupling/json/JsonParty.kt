@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)
package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonParty(
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
data class JsonPartyRecord(
    val id: PartyId,
    val pairingRule: Int = PairingRule.toValue(PairingRule.LongestTime),
    val badgesEnabled: Boolean = false,
    val defaultBadgeName: String = "Default",
    val alternateBadgeName: String = "Alternate",
    val email: String? = null,
    val name: String? = null,
    val callSignsEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
    val animationSpeed: Double = 1.0,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: DateTime,
)

fun Party.toSerializable() = JsonParty(
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

fun Record<Party>.toSerializable() = JsonPartyRecord(
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
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonParty.toModel(): Party = Party(
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

fun JsonPartyRecord.toModelRecord(): Record<Party> = Record(
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
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp
)
