package com.zegreatrob.coupling.json

import korlibs.time.DateTime
import korlibs.time.ISO8601
import korlibs.time.parse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class DateTimeSerializer : KSerializer<DateTime> {
    override val descriptor = PrimitiveSerialDescriptor("DateTime", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = decoder.decodeString().parseISODateTime()
    override fun serialize(encoder: Encoder, value: DateTime) = encoder.encodeString(value.toCustomIsoString())
}

private fun String.parseISODateTime() = ISO8601.DATETIME_UTC_COMPLETE_FRACTION.parse(this).local
private fun DateTime.toCustomIsoString() = format(ISO8601.DATETIME_COMPLETE) + zoneAndMillis(this)
private fun zoneAndMillis(soyDate: DateTime) = ".${soyDate.millisecondsString()}Z"
private fun DateTime.millisecondsString() = milliseconds.toString().padStart(3, '0')
