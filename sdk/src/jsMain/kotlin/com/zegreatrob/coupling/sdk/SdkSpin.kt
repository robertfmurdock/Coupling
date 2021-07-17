package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minjson.at
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json
import kotlin.js.json

interface SdkSpin : GqlSyntax {

    suspend fun requestSpin(tribeId: TribeId, players: List<Player>, pins: List<Pin>) =
        performQuery(json("query" to Mutations.spin, "variables" to spinBody(players, pins, tribeId)))
            .unsafeCast<Json>()
            .at<Json>("/data/data/spin/result")!!
            .fromJsonDynamic<JsonPairAssignmentDocument>().toModel()

    private fun spinBody(players: List<Player>, pins: List<Pin>, tribeId: TribeId) = json(
        "input" to json(
            "players" to players.map { it.toSerializable().toJsonDynamic() }.toTypedArray(),
            "pins" to pins.map { couplingJsonFormat.encodeToDynamic(it.toPinInput()) }.toTypedArray(),
            "tribeId" to tribeId.value
        )
    )

}
