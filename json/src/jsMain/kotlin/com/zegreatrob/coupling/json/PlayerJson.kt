package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.player.Player
import kotlin.js.Json

fun Player.toJson(): Json = emptyArray<Pair<String, Any?>>()
    .plus("_id", id)
    .plus("name", name)
    .plus("email", email)
    .plus("badge", badge)
    .plus("callSignAdjective", callSignAdjective)
    .plus("callSignNoun", callSignNoun)
    .plus("imageURL", imageURL)
    .pairsToJson()

fun Array<Pair<String, Any?>>.plus(key: String, value: Any?) = plus(Pair(key, value))

fun List<Player>.toJsonArray() = map { it.toJson() }
    .toTypedArray()

@Suppress("UNCHECKED_CAST")
fun Json.toPlayer(): Player = Player(
    id = stringValue("_id"),
    badge = this["badge"]?.toIntFromStringOrInt(),
    name = stringValue("name"),
    email = stringValue("email"),
    callSignAdjective = stringValue("callSignAdjective"),
    callSignNoun = stringValue("callSignNoun"),
    imageURL = stringValue("imageURL")
)

val playerJsonKeys = Player()
    .toJson()
    .getKeys()
