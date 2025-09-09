package com.zegreatrob.coupling.sdk

import com.example.DisconnectUserMutation
import com.example.type.DisconnectUserInput
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.user.DisconnectUserCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkDisconnectUserCommandDispatcher :
    DisconnectUserCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DisconnectUserCommand) = apolloMutation(DisconnectUserMutation(DisconnectUserInput(command.email)))
        .data
        ?.disconnectUser
        .let { VoidResult.Accepted }
}
