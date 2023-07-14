package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkDeletePinCommandDispatcher : DeletePinCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: DeletePinCommand) = with(command) {
        (doQuery(Mutation.deletePin, deletePinInput(command.partyId, pinId), "deletePin", ::passThrough) ?: false)
            .voidResult()
    }
}

private fun passThrough(it: Boolean?): Boolean? = it
private fun deletePinInput(partyId: PartyId, pinId: String) = mapOf("partyId" to partyId.value, "pinId" to pinId)
