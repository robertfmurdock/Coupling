package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

val couplingJsonFormat = kotlinx.serialization.json.Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

inline fun <reified T> T.toJsonString() = couplingJsonFormat.encodeToString(this)
inline fun <reified T> String.fromJsonString() = couplingJsonFormat.decodeFromString<T>(this)


inline fun <reified T> T.toJsonDynamic() = couplingJsonFormat.encodeToDynamic(this)
inline fun <reified T> Json.fromJsonDynamic() = couplingJsonFormat.decodeFromDynamic<T>(this)

val playerJsonKeys = Player(
    id = "1",
    badge = 1,
    name = "stub",
    email = "stub",
    callSignAdjective = "stub",
    callSignNoun = "stub",
    imageURL = "stub",
)
    .toSerializable()
    .toJsonDynamic()
    .unsafeCast<Json>()
    .getKeys()

val playerRecordJsonKeys = TribeRecord(
    TribeId("").with(
        Player(
            id = "1",
            badge = 1,
            name = "stub",
            email = "stub",
            callSignAdjective = "stub",
            callSignNoun = "stub",
            imageURL = "stub",
        )
    ),
    ""
)
    .toSerializable()
    .toJsonDynamic()
    .unsafeCast<Json>()
    .getKeys()
