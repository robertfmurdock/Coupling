package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.user.DisconnectUserCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.DisconnectUserMutation
import com.zegreatrob.coupling.sdk.schema.type.DisconnectUserInput

interface SdkDisconnectUserCommandDispatcher :
    DisconnectUserCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DisconnectUserCommand) = DisconnectUserMutation(DisconnectUserInput(command.email)).execute()
        .data
        ?.disconnectUser
        .let { VoidResult.Accepted }
}
