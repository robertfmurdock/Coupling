package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.defaultParty
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.text.NotBlankString
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

fun Record<PartyDetails>.toSerializable() = GqlPartyDetails(
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

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPartyDetails.toModel(): PartyDetails = PartyDetails(
    id = id,
    pairingRule = PairingRule.fromValue(pairingRule),
    defaultBadgeName = defaultBadgeName,
    alternateBadgeName = alternateBadgeName,
    email = email,
    name = name,
    badgesEnabled = badgesEnabled == true,
    callSignsEnabled = callSignsEnabled == true,
    animationEnabled = animationsEnabled != false,
    animationSpeed = animationSpeed ?: 1.0,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPartyDetails.toModelRecord(): Record<PartyDetails>? {
    return Record(
        data = PartyDetails(
            id = id,
            pairingRule = PairingRule.fromValue(pairingRule),
            defaultBadgeName = defaultBadgeName,
            alternateBadgeName = alternateBadgeName,
            email = email,
            name = name,
            badgesEnabled = badgesEnabled == true,
            callSignsEnabled = callSignsEnabled == true,
            animationEnabled = animationsEnabled != false,
            animationSpeed = animationSpeed ?: 1.0,
        ),
        modifyingUserId = modifyingUserEmail ?: return null,
        isDeleted = isDeleted ?: return null,
        timestamp = timestamp ?: return null,
    )
}

typealias PartyIdString =
    @Serializable(PartyIdSerializer::class)
    PartyId

object PartyIdSerializer : KSerializer<PartyId> {
    private val delegateSerializer = NotBlankString.serializer()

    override val descriptor = SerialDescriptor(
        serialName = "com.zegreatrob.coupling.model.party.PartyId",
        original = delegateSerializer.descriptor,
    )

    override fun serialize(
        encoder: Encoder,
        value: PartyId,
    ) = delegateSerializer.serialize(encoder, value.value)

    override fun deserialize(decoder: Decoder): PartyId = PartyId(delegateSerializer.deserialize(decoder))
}
