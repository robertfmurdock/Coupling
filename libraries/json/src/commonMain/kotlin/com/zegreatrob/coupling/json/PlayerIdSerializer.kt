package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.player.PlayerId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.text.NotBlankString

@Suppress("unused")
typealias PlayerIdString =
    @Serializable(PlayerIdSerializer::class)
    PlayerId

object PlayerIdSerializer : KSerializer<PlayerId> {
    private val delegateSerializer = NotBlankString.serializer()

    override val descriptor = SerialDescriptor(
        serialName = "com.zegreatrob.coupling.model.player.PlayerId",
        original = delegateSerializer.descriptor,
    )

    override fun serialize(
        encoder: Encoder,
        value: PlayerId,
    ) = delegateSerializer.serialize(encoder, value.value)

    override fun deserialize(decoder: Decoder): PlayerId = PlayerId(delegateSerializer.deserialize(decoder))
}
