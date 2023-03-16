package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.RequestSpinAction
import com.zegreatrob.coupling.action.RequestSpinActionDispatcher
import com.zegreatrob.coupling.json.SpinInput
import com.zegreatrob.coupling.json.SpinOutput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

interface SdkSpin : RequestSpinActionDispatcher, GqlSyntax, GraphQueries {

    override suspend fun perform(action: RequestSpinAction): PairAssignmentDocument =
        doQuery(
            mutations.spin,
            action.spinInput(),
            "spin",
            ::toOutput,
        )!!

    private fun toOutput(at: SpinOutput) = at.result.toModel()
}

fun RequestSpinAction.spinInput() = SpinInput(
    players = players.map(Player::toSerializable),
    pins = pins.map(Pin::toSerializable),
    partyId = partyId,
)
