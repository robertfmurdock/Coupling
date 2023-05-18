package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.json.SpinInput
import com.zegreatrob.coupling.json.SpinOutput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSpin :
    RequestSpinAction.Dispatcher,
    GqlSyntax {

    override suspend fun perform(action: RequestSpinAction): PairAssignmentDocument =
        doQuery(
            Mutation.spin,
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
