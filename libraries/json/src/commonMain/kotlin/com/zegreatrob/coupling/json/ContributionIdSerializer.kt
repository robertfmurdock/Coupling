package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.ContributionId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.text.NotBlankString

@Suppress("unused")
typealias ContributionIdString =
    @Serializable(ContributionIdSerializer::class)
    ContributionId

object ContributionIdSerializer : KSerializer<ContributionId> {
    private val delegateSerializer = NotBlankString.serializer()

    override val descriptor = SerialDescriptor(
        serialName = "com.zegreatrob.coupling.model.ContributionId",
        original = delegateSerializer.descriptor,
    )

    override fun serialize(
        encoder: Encoder,
        value: ContributionId,
    ) = delegateSerializer.serialize(encoder, value.value)

    override fun deserialize(decoder: Decoder): ContributionId = ContributionId(delegateSerializer.deserialize(decoder))
}
