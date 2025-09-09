package com.zegreatrob.coupling.sdk

import com.example.GrantDiscordAccessMutation
import com.example.type.GrantDiscordAccessInput
import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkGrantDiscordAccess :
    GrantDiscordAccessCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: GrantDiscordAccessCommand): VoidResult = apolloMutation(GrantDiscordAccessMutation(command.grantDiscordAccessInput()))
        .data?.grantDiscordAccess?.toVoidResult() ?: CommandResult.Unauthorized
}

fun GrantDiscordAccessCommand.grantDiscordAccessInput() = GrantDiscordAccessInput(
    code = code,
    partyId = partyId,
    guildId = guildId,
)
