package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

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
