package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pin.PinId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.text.NotBlankString

typealias PinIdString =
    @Serializable(PinIdSerializer::class)
    PinId

object PinIdSerializer : KSerializer<PinId> {
    private val delegateSerializer = NotBlankString.serializer()

    override val descriptor = SerialDescriptor(
        serialName = "com.zegreatrob.coupling.model.player.PinId",
        original = delegateSerializer.descriptor,
    )

    override fun serialize(
        encoder: Encoder,
        value: PinId,
    ) = delegateSerializer.serialize(encoder, value.value)

    override fun deserialize(decoder: Decoder): PinId = PinId(delegateSerializer.deserialize(decoder))
}
