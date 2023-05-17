package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand

interface SdkDeletePartyCommandDispatcher : DeletePartyCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: DeletePartyCommand) = with(command) {
        doQuery(
            Mutation.deleteParty,
            mapOf("partyId" to partyId.value),
            "deleteParty",
        ) { it: Boolean? -> it }
            .let { it ?: false }
            .deletionResult("Party")
    }
}
