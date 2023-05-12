package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pin.PinDelete

interface SdkPinDelete : PinDelete, GqlSyntax, GraphQueries {
    override suspend fun deletePin(partyId: PartyId, pinId: String) =
        doQuery(mutations.deletePin, deletePinInput(partyId, pinId), "deletePin", ::passThrough) ?: false
}

private fun passThrough(it: Boolean?): Boolean? = it
private fun deletePinInput(partyId: PartyId, pinId: String) = mapOf("partyId" to partyId.value, "pinId" to pinId)

interface SdkDeletePinCommandDispatcher : DeletePinCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: DeletePinCommand) = with(command) {
        doQuery(Mutation.deletePin, deletePinInput(command.partyId, pinId), "deletePin", ::passThrough)
            .let { it ?: false }
            .deletionResult("Pin")
    }
}
