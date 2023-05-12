package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.player.PlayerDelete

interface SdkPlayerDeleter : PlayerDelete, GqlSyntax, GraphQueries {
    override suspend fun deletePlayer(partyId: PartyId, playerId: String) = doQuery(
        mutations.deletePlayer,
        mapOf("partyId" to partyId.value, "playerId" to playerId),
        "deletePlayer",
    ) { it: Boolean? -> it } ?: false
}

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
