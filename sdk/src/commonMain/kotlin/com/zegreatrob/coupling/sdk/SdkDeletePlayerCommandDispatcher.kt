package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.json.GqlDeletePlayerInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkDeletePlayerCommandDispatcher :
    DeletePlayerCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePlayerCommand) = with(command) {
        doQuery(
            Mutation.deletePlayer,
            GqlDeletePlayerInput(partyId = partyId, playerId = playerId),
            "deletePlayer",
        ) { it: Boolean? -> it }
            ?.voidResult()
            ?: CommandResult.Unauthorized
    }
}
