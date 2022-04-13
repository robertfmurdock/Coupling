package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.SpinDispatcher
import com.zegreatrob.coupling.json.SpinInput
import com.zegreatrob.coupling.json.SpinOutput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.PartyId

interface SdkSpin : SpinDispatcher, GqlSyntax, GraphQueries {

    override suspend fun requestSpin(tribeId: PartyId, players: List<Player>, pins: List<Pin>) = doQuery(
        mutations.spin,
        SpinInput(
            players = players.map(Player::toSerializable),
            pins = pins.map(Pin::toSerializable),
            tribeId = tribeId
        ),
        "spin",
        ::toOutput
    )!!

    private fun toOutput(at: SpinOutput) = at.result.toModel()

}
