package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.user.DisconnectUserCommand
import com.zegreatrob.coupling.json.GqlDisconnectUserInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkDisconnectUserCommandDispatcher :
    DisconnectUserCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DisconnectUserCommand) = doQuery(
        Mutation.disconnectUser,
        GqlDisconnectUserInput(command.email),
    )
        .parseMutationResult()
        .disconnectUser
        .let { VoidResult.Accepted }
}
