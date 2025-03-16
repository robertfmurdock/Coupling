package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.text.NotBlankString

typealias PairAssignmentDocumentIdString =
    @Serializable(PairAssignmentDocumentIdSerializer::class)
    PairAssignmentDocumentId

object PairAssignmentDocumentIdSerializer : KSerializer<PairAssignmentDocumentId> {
    private val delegateSerializer = NotBlankString.Companion.serializer()

    override val descriptor = SerialDescriptor(
        serialName = "com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId",
        original = delegateSerializer.descriptor,
    )

    override fun serialize(
        encoder: Encoder,
        value: PairAssignmentDocumentId,
    ) = delegateSerializer.serialize(encoder, value.value)

    override fun deserialize(decoder: Decoder): PairAssignmentDocumentId = PairAssignmentDocumentId(delegateSerializer.deserialize(decoder))
}
