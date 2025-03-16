package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.json.GqlSpinInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery
import kotlinx.serialization.json.JsonElement

interface SdkSpin :
    SpinCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: SpinCommand): SpinCommand.Result = doQuery(
        mutation = Mutation.spin,
        input = command.spinInput(),
        resultName = "spin",
        toOutput = { _: JsonElement -> SpinCommand.Result.Success },
    ) ?: CommandResult.Unauthorized
}

fun SpinCommand.spinInput() = GqlSpinInput(
    partyId = partyId,
    playerIds = playerIds.toList(),
    pinIds = pinIds,
)
