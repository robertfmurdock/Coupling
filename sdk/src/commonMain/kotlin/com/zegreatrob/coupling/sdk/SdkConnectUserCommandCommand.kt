package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.user.ConnectUserCommand
import com.zegreatrob.coupling.json.GqlConnectUserInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkConnectUserCommandCommand :
    ConnectUserCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: ConnectUserCommand) = doQuery(Mutation.connectUser, GqlConnectUserInput(command.token))
        .parseMutationResult()
        .connectUser
}
