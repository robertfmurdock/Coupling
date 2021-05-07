package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.Json
import kotlin.js.json

interface SdkSpin : GqlSyntax {

    suspend fun requestSpin(
        tribeId: TribeId,
        players: List<Player>,
        pins: List<Pin>
    ) = performQuery(
        json(
            "query" to "mutation spin(\$input: SpinInput!) { spin(input: \$input) {  result { _id, date, pairs { players { _id, name, email, badge, callSignAdjective, callSignNoun, imageURL, pins { _id,icon,name }  }, pins { _id,icon,name } } } } }",
            "variables" to spinBody(players, pins, tribeId)
        )
    )
        .data.data.spin.result.unsafeCast<Json>()
        .toPairAssignmentDocument()

    private fun spinBody(players: List<Player>, pins: List<Pin>, tribeId: TribeId) = json(
        "input" to json(
            "players" to players.map { it.toJson() }.toTypedArray(),
            "pins" to pins.map { it.toJson() }.toTypedArray(),
            "tribeId" to tribeId.value
        )
    )
}