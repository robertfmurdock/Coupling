package com.zegreatrob.coupling.sdk

import com.example.SpinMutation
import com.example.type.SpinInput
import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkSpin :
    SpinCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: SpinCommand): SpinCommand.Result = apolloMutation(SpinMutation(command.spinInput()))
        .data
        ?.spin
        ?.let { SpinCommand.Result.Success }
        ?: CommandResult.Unauthorized
}

fun SpinCommand.spinInput() = SpinInput(
    partyId = partyId,
    playerIds = playerIds.toList(),
    pinIds = pinIds,
)
