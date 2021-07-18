package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minjson.at
import kotlin.js.Json

interface SdkSpin : GqlSyntax {

    suspend fun requestSpin(tribeId: TribeId, players: List<Player>, pins: List<Pin>) = performQuery(
        Mutations.spin,
        SpinInput(
            players = players.map(Player::toSerializable),
            pins = pins.map(Pin::toSerializable),
            tribeId = tribeId.value
        )
    ).unsafeCast<Json>()
        .at<Json>("/data/data/spin/result")!!
        .fromJsonDynamic<JsonPairAssignmentDocument>()
        .toModel()

}
