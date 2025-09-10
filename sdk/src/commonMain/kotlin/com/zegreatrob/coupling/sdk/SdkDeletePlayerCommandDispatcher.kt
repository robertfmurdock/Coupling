package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.DeletePlayerMutation
import com.zegreatrob.coupling.sdk.schema.type.DeletePlayerInput

interface SdkDeletePlayerCommandDispatcher :
    DeletePlayerCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePlayerCommand) = with(command) {
        DeletePlayerMutation(DeletePlayerInput(partyId = partyId, playerId = playerId)).execute()
            .data
            ?.deletePlayer
            ?.voidResult()
            ?: CommandResult.Unauthorized
    }
}
