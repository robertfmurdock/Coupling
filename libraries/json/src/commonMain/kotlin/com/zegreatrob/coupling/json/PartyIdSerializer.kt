package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.text.NotBlankString

typealias PartyIdString =
    @Serializable(PartyIdSerializer::class)
    PartyId

object PartyIdSerializer : KSerializer<PartyId> {
    private val delegateSerializer = NotBlankString.Companion.serializer()

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
