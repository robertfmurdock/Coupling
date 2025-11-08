package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.json.JsonPairingSet
import com.zegreatrob.coupling.json.JsonPartyDetails
import com.zegreatrob.coupling.json.JsonPlayer
import com.zegreatrob.coupling.json.JsonPlayerData
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json

actual fun loadJsonPartySetup(fileResource: String): PartySetup = loadResource<Any>(fileResource).unsafeCast<Json>()
    .let<Json, JsonPartySetup>(couplingJsonFormat::decodeFromDynamic)
    .run {
        PartySetup(party.toModel(), players.map(JsonPlayer::toModel), history.map(JsonPairingSet::toModel))
    }

@Serializable
data class JsonPartySetup(
    val party: JsonPartyDetails,
    val players: List<JsonPlayerData>,
    val history: List<JsonPairingSet>,
)
