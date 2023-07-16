package com.zegreatrob.coupling.json

import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class DateTimeSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("DateTime", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = decoder.decodeString().toInstant()
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
}
