package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.json.SpinInput
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery
import kotlinx.serialization.json.JsonElement

interface SdkSpin :
    SpinCommand.Dispatcher,
    GqlSyntax {

    override suspend fun perform(command: SpinCommand): VoidResult =
        doQuery(
            mutation = Mutation.spin,
            input = command.spinInput(),
            resultName = "spin",
            toOutput = { _: JsonElement -> VoidResult.Accepted },
        ) ?: CommandResult.Unauthorized
}

fun SpinCommand.spinInput() = SpinInput(
    partyId = partyId,
    playerIds = playerIds,
    pinIds = pinIds,
)
