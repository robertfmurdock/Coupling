package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.DeletePartyMutation
import com.zegreatrob.coupling.sdk.schema.type.DeletePartyInput

interface SdkDeletePartyCommandDispatcher :
    DeletePartyCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePartyCommand) = with(command) {
        DeletePartyMutation(DeletePartyInput(partyId)).execute()
            .data?.deleteParty
            .let { it == true }
            .voidResult()
    }
}
