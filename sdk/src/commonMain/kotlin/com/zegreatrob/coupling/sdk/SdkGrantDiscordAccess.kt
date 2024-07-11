package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.json.GrantDiscordAccessInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkGrantDiscordAccess :
    GrantDiscordAccessCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: GrantDiscordAccessCommand): VoidResult = doQuery(
        mutation = Mutation.grantDiscordAccess,
        input = command.grantDiscordAccessInput(),
        resultName = "grantDiscordAccess",
        toOutput = Boolean?::toVoidResult,
    ) ?: CommandResult.Unauthorized
}

fun GrantDiscordAccessCommand.grantDiscordAccessInput() = GrantDiscordAccessInput(
    code = code,
    partyId = partyId.value,
    guildId = guildId,
)
