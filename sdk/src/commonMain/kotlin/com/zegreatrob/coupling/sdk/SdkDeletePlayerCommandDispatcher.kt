package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkDeletePlayerCommandDispatcher : DeletePlayerCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: DeletePlayerCommand) = with(command) {
        doQuery(
            Mutation.deletePlayer,
            mapOf("partyId" to partyId.value, "playerId" to playerId),
            "deletePlayer",
        ) { it: Boolean? -> it }
            .let { it ?: false }
            .deletionResult("player")
    }
}
