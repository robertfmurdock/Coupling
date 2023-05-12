package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyDelete

interface SdkPartyDelete : PartyDelete, GqlSyntax, GraphQueries {
    override suspend fun deleteIt(partyId: PartyId): Boolean = doQuery(
        mutations.deleteParty,
        mapOf("partyId" to partyId.value),
        "deleteParty",
    ) { it: Boolean? -> it } ?: false
}

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
