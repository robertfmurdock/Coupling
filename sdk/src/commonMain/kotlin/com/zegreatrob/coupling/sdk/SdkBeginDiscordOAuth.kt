package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.BeginDiscordOAuthCommand
import com.zegreatrob.coupling.action.DiscordOAuthState
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.BeginDiscordOAuthMutation
import com.zegreatrob.coupling.sdk.schema.type.BeginDiscordOAuthInput

interface SdkBeginDiscordOAuth :
    BeginDiscordOAuthCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: BeginDiscordOAuthCommand): DiscordOAuthState? = BeginDiscordOAuthMutation(BeginDiscordOAuthInput(command.partyId, command.challenge)).execute()
        .data?.beginDiscordOAuth?.let { DiscordOAuthState(it.state, it.expiresAt) }
}
