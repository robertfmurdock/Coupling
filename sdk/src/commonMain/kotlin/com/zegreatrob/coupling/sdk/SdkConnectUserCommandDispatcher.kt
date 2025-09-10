package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.user.ConnectUserCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.ConnectUserMutation
import com.zegreatrob.coupling.sdk.schema.type.ConnectUserInput

interface SdkConnectUserCommandDispatcher :
    ConnectUserCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: ConnectUserCommand) = ConnectUserMutation(
        ConnectUserInput(
            command.token,
        ),
    ).execute().data?.connectUser
}
