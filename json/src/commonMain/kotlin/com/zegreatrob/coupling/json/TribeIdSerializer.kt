package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class TribeIdSerializer: KSerializer<TribeId> {
    override val descriptor = PrimitiveSerialDescriptor("TribeId", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = TribeId(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: TribeId) = encoder.encodeString(value.value)
}