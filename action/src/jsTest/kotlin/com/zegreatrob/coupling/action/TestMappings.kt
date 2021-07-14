package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json

actual fun loadJsonTribeSetup(fileResource: String): TribeSetup = loadResource<Any>(fileResource)
    .unsafeCast<Json>()
    .let { couplingJsonFormat.decodeFromDynamic<JsonTribeSetup>(it) }
    .let {
        TribeSetup(
            tribe = it.tribe.toModel(),
            players = it.players.map(JsonPlayer::toModel),
            history = it.history.map(JsonPairAssignmentDocument::toModel)
        )
    }

@Serializable
data class JsonTribeSetup(
    val tribe: JsonTribe,
    val players: List<JsonPlayerData>,
    val history: List<JsonPairAssignmentDocument>,
)
