package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.GrantDiscordAccessMutation
import com.zegreatrob.coupling.sdk.schema.type.GrantDiscordAccessInput

interface SdkGrantDiscordAccess :
    GrantDiscordAccessCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: GrantDiscordAccessCommand): VoidResult = GrantDiscordAccessMutation(command.grantDiscordAccessInput()).execute()
        .data?.grantDiscordAccess?.toVoidResult() ?: CommandResult.Unauthorized
}

internal fun GrantDiscordAccessCommand.grantDiscordAccessInput() = GrantDiscordAccessInput(
    code = code,
    partyId = partyId,
    guildId = guildId,
)
