package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.user.UserId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.text.NotBlankString

typealias UserIdString =
    @Serializable(UserIdSerializer::class)
    UserId

object UserIdSerializer : KSerializer<UserId> {
    private val delegateSerializer = NotBlankString.serializer()

    override val descriptor = SerialDescriptor(
        serialName = "com.zegreatrob.coupling.model.user.UserId",
        original = delegateSerializer.descriptor,
    )

    override fun serialize(
        encoder: Encoder,
        value: UserId,
    ) = delegateSerializer.serialize(encoder, value.value)

    override fun deserialize(decoder: Decoder): UserId = UserId(delegateSerializer.deserialize(decoder))
}
