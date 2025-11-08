package com.zegreatrob.coupling.sdk.adapter

import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.user.UserId
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun <T> serializationAdapter(init: (NotBlankString) -> T, toValue: (T) -> NotBlankString): Adapter<T> = object : Adapter<T> {
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): T = reader.nextString()?.toNotBlankString()?.getOrNull()?.let(init)
        ?: throw NullPointerException("Json value was null.")

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: T) {
        toValue(value).let { writer.value(it.toString()) }
    }
}

val contributionAdapter = serializationAdapter(::ContributionId, ContributionId::value)
val partyIdAdapter = serializationAdapter(::PartyId, PartyId::value)
val playerIdAdapter = serializationAdapter(::PlayerId, PlayerId::value)
val pinIdAdapter = serializationAdapter(::PinId, PinId::value)
val secretIdAdapter = serializationAdapter(::SecretId, SecretId::value)
val userIdAdapter = serializationAdapter(::UserId, UserId::value)
val pairingSetIdAdapter = serializationAdapter(::PairingSetId, PairingSetId::value)
val notBlankStringAdapter = serializationAdapter({ it }, { it })

@ExperimentalTime
@Suppress("unused")
object KotlinDurationAdapter : Adapter<Duration> {
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Duration = Duration.parse(reader.nextString()!!)

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Duration) {
        writer.value(value.toIsoString())
    }
}
