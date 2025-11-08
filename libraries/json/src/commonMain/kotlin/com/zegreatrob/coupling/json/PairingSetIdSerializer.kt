package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.text.NotBlankString

typealias PairingSetIdString =
    @Serializable(PairingSetIdSerializer::class)
    PairingSetId

object PairingSetIdSerializer : KSerializer<PairingSetId> {
    private val delegateSerializer = NotBlankString.serializer()

    override val descriptor = SerialDescriptor(
        serialName = "com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId",
        original = delegateSerializer.descriptor,
    )

    override fun serialize(
        encoder: Encoder,
        value: PairingSetId,
    ) = delegateSerializer.serialize(encoder, value.value)

    override fun deserialize(decoder: Decoder): PairingSetId = PairingSetId(delegateSerializer.deserialize(decoder))
}
