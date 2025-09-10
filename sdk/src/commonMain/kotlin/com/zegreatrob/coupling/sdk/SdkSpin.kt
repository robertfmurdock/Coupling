package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.SpinMutation
import com.zegreatrob.coupling.sdk.schema.type.SpinInput

interface SdkSpin :
    SpinCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: SpinCommand): SpinCommand.Result = SpinMutation(command.spinInput())
        .execute()
        .data
        ?.spin
        ?.let { SpinCommand.Result.Success }
        ?: CommandResult.Unauthorized
}

internal fun SpinCommand.spinInput() = SpinInput(
    partyId = partyId,
    playerIds = playerIds.toList(),
    pinIds = pinIds,
)
