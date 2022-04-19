package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

val couplingJsonFormat = kotlinx.serialization.json.Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

inline fun <reified T> T.toJsonString() = couplingJsonFormat.encodeToString(this)
inline fun <reified T> String.fromJsonString() = couplingJsonFormat.decodeFromString<T>(this)
inline fun <reified T> T.toJsonElement() = couplingJsonFormat.encodeToJsonElement(this)
inline fun <reified T> JsonElement.fromJsonElement() = couplingJsonFormat.decodeFromJsonElement<T>(this)

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
    .toJsonElement()
    .jsonObject.keys

val playerRecordJsonKeys = PartyRecord(
    PartyId("").with(
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
    .toJsonElement()
    .jsonObject
    .keys
