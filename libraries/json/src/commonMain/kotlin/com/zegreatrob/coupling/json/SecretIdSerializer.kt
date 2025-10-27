package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.SecretId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.text.NotBlankString

@Suppress("unused")
typealias SecretIdString =
    @Serializable(SecretIdSerializer::class)
    SecretId

object SecretIdSerializer : KSerializer<SecretId> {
    private val delegateSerializer = NotBlankString.serializer()

    override val descriptor = SerialDescriptor(
        serialName = "com.zegreatrob.coupling.model.party.SecretId",
        original = delegateSerializer.descriptor,
    )

    override fun serialize(
        encoder: Encoder,
        value: SecretId,
    ) = delegateSerializer.serialize(encoder, value.value)

    override fun deserialize(decoder: Decoder): SecretId = SecretId(delegateSerializer.deserialize(decoder))
}
