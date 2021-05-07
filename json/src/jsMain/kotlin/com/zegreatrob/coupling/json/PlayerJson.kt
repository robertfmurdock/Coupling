package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.player.player
import kotlin.js.Json

fun Player.toJson(): Json = emptyArray<Pair<String, Any?>>()
    .plus("_id", id)
    .plus("name", name)
    .plus("email", email)
    .plus("badge", "$badge")
    .plus("callSignAdjective", callSignAdjective)
    .plus("callSignNoun", callSignNoun)
    .plus("imageURL", imageURL)
    .pairsToJson()

fun Array<Pair<String, Any?>>.plus(key: String, value: Any?) = plus(Pair(key, value))

fun List<Record<TribeIdPlayer>>.toJsonArray() = map { it.toJson().add(it.data.player.toJson()) }
    .toTypedArray()

@Suppress("UNCHECKED_CAST")
fun Json.toPlayer(): Player = Player(
    id = stringValue("_id") ?: "",
    badge = this["badge"]?.toIntFromStringOrInt() ?: defaultPlayer.badge,
    name = stringValue("name") ?: defaultPlayer.name,
    email = stringValue("email") ?: defaultPlayer.email,
    callSignAdjective = stringValue("callSignAdjective") ?: defaultPlayer.callSignAdjective,
    callSignNoun = stringValue("callSignNoun") ?: defaultPlayer.callSignNoun,
    imageURL = stringValue("imageURL")
)

val playerJsonKeys
    get() = Player()
        .toJson()
        .getKeys()

val playerRecordJsonKeys = playerJsonKeys + recordJsonKeys