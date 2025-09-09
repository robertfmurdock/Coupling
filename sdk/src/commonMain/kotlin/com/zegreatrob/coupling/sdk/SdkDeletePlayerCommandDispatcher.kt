package com.zegreatrob.coupling.sdk

import com.example.DeletePlayerMutation
import com.example.type.DeletePlayerInput
import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkDeletePlayerCommandDispatcher :
    DeletePlayerCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePlayerCommand) = with(command) {
        apolloMutation(DeletePlayerMutation(DeletePlayerInput(partyId = partyId, playerId = playerId)))
            .data
            ?.deletePlayer
            ?.voidResult()
            ?: CommandResult.Unauthorized
    }
}
