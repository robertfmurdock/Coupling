package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.player.player
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

val format = kotlinx.serialization.json.Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@ExperimentalSerializationApi
fun Player.toJson(): Json = format.encodeToDynamic(toSerializationType()).unsafeCast<Json>()

private fun Player.toSerializationType() = JsonPlayer(
    id = id,
    name = name,
    email = email,
    badge = "$badge",
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL
)

fun Array<Pair<String, Any?>>.plus(key: String, value: Any?) = plus(Pair(key, value))

@ExperimentalSerializationApi
fun List<Record<TribeIdPlayer>>.toJsonArray() = also { println("record is $it") }.map { it.toJson().add(it.data.player.toJson()) }
    .toTypedArray()
    .also { println("json returned is ${JSON.stringify(it)}") }

@ExperimentalSerializationApi
@Suppress("UNCHECKED_CAST")
fun Json.toPlayer(): Player = format.decodeFromDynamic<JsonPlayer>(asDynamic()).fromSerializable()

private fun JsonPlayer.fromSerializable(): Player = Player(
    id = id,
    badge = badge.toIntOrNull() ?: defaultPlayer.badge,
    name = name,
    email = email,
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL
)

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

@Serializable
data class JsonPlayer(
    val id: String,
    val name: String = defaultPlayer.name,
    val email: String = defaultPlayer.email,
    val badge: String = "${defaultPlayer.badge}",
    val callSignAdjective: String = defaultPlayer.callSignAdjective,
    val callSignNoun: String = defaultPlayer.callSignNoun,
    val imageURL: String? = defaultPlayer.imageURL,
)
