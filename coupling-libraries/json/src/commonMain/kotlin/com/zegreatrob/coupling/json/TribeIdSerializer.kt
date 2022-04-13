package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class TribeIdSerializer: KSerializer<PartyId> {
    override val descriptor = PrimitiveSerialDescriptor("PartyId", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = PartyId(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: PartyId) = encoder.encodeString(value.value)
}