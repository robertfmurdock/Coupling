package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = PairAssignmentDocumentId::class)
class PairAssignmentDocumentIdSerializer : KSerializer<PairAssignmentDocumentId> {
    override val descriptor = PrimitiveSerialDescriptor("PairAssignmentDocumentId", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = PairAssignmentDocumentId(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: PairAssignmentDocumentId) = encoder.encodeString(value.value)
}
