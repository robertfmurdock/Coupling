package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.json.GqlDeletePartyInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkDeletePartyCommandDispatcher :
    DeletePartyCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePartyCommand) = with(command) {
        doQuery(
            Mutation.deleteParty,
            GqlDeletePartyInput(partyId.value.toString()),
            "deleteParty",
        ) { it: Boolean? -> it }
            .let { it == true }
            .voidResult()
    }
}
