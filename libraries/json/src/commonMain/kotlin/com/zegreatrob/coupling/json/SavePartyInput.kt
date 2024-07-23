@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class SavePartyInput(
    val partyId: PartyId,
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

@Serializable
data class SaveSlackIntegrationInput(
    val partyId: String,
    val team: String,
    val channel: String,
)

@Serializable
data class JsonSecretToken(
    val secretId: String,
    val description: String,
    val createdTimestamp: Instant,
    val secretToken: String,
    val lastUsedTimestamp: Instant?,
)
