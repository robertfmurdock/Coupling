package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.SpinInput
import com.zegreatrob.coupling.json.SpinOutput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId

interface SdkSpin : GqlSyntax {

    suspend fun requestSpin(tribeId: TribeId, players: List<Player>, pins: List<Pin>) = performQuery(
        Mutations.spin,
        SpinInput(
            players = players.map(Player::toSerializable),
            pins = pins.map(Pin::toSerializable),
            tribeId = tribeId.value
        ),
        "spin",
        ::toOutput
    )!!

    private fun toOutput(at: SpinOutput) = at.result.toModel()

}
