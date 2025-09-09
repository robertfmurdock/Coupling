package com.zegreatrob.coupling.sdk

import com.example.ConnectUserMutation
import com.example.type.ConnectUserInput
import com.zegreatrob.coupling.action.user.ConnectUserCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkConnectUserCommandDispatcher :
    ConnectUserCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: ConnectUserCommand) = apolloMutation(
        ConnectUserMutation(
            ConnectUserInput(
                command.token,
            ),
        ),
    ).data?.connectUser
}
