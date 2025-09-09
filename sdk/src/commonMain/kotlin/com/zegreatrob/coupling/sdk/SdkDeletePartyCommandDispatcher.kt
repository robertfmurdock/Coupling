package com.zegreatrob.coupling.sdk

import com.example.DeletePartyMutation
import com.example.type.DeletePartyInput
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkDeletePartyCommandDispatcher :
    DeletePartyCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePartyCommand) = with(command) {
        apolloMutation(DeletePartyMutation(DeletePartyInput(partyId)))
            .data?.deleteParty
            .let { it == true }
            .voidResult()
    }
}
