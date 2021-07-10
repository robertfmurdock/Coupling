package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

private val format = kotlinx.serialization.json.Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

@ExperimentalSerializationApi
fun Player.toJson(): Json = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun Pin.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun Json.toPlayer(): Player = format.decodeFromDynamic<JsonPlayer>(asDynamic()).toModel()

@ExperimentalSerializationApi
fun Json.toPin() = format.decodeFromDynamic<JsonPin>(asDynamic()).toModel()

@ExperimentalSerializationApi
fun Json.toTribe() = format.decodeFromDynamic<JsonTribe>(asDynamic()).toModel()

@ExperimentalSerializationApi
fun Tribe.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Array<Pair<String, Any?>>.plus(key: String, value: Any?) = plus(Pair(key, value))

@ExperimentalSerializationApi
fun List<Record<TribeIdPlayer>>.toJsonArray() = map { it.toJson().add(it.data.player.toJson()) }
    .toTypedArray()

@ExperimentalSerializationApi
fun List<Pin>.toJson(): Array<Json> = map { it.toJson() }
    .toTypedArray()

@ExperimentalSerializationApi
fun List<Record<TribeIdPin>>.toJsonArray() = map { it.toJson().add(it.data.pin.toJson()) }
    .toTypedArray()

@ExperimentalSerializationApi
fun Array<Json>.toPins() = map { it.toPin() }

@ExperimentalSerializationApi
val playerJsonKeys = Player(
    id = "1",
    badge = 1,
    name = "stub",
    email = "stub",
    callSignAdjective = "stub",
    callSignNoun = "stub",
    imageURL = "stub",
)
    .toJson()
    .getKeys()

@ExperimentalSerializationApi
val playerRecordJsonKeys = playerJsonKeys + recordJsonKeys
