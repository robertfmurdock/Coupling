package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json

actual fun loadJsonTribeSetup(fileResource: String): TribeSetup = loadResource<Any>(fileResource).unsafeCast<Json>()
    .let<Json, JsonTribeSetup>(couplingJsonFormat::decodeFromDynamic)
    .run {
        TribeSetup(tribe.toModel(), players.map(JsonPlayer::toModel), history.map(JsonPairAssignmentDocument::toModel))
    }

@Serializable
data class JsonTribeSetup(
    val tribe: JsonTribe,
    val players: List<JsonPlayerData>,
    val history: List<JsonPairAssignmentDocument>,
)
