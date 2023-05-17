package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.model.party.PartyId

interface SdkDeletePinCommandDispatcher : DeletePinCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: DeletePinCommand) = with(command) {
        doQuery(Mutation.deletePin, deletePinInput(command.partyId, pinId), "deletePin", ::passThrough)
            .let { it ?: false }
            .deletionResult("Pin")
    }
}
private fun passThrough(it: Boolean?): Boolean? = it
private fun deletePinInput(partyId: PartyId, pinId: String) = mapOf("partyId" to partyId.value, "pinId" to pinId)
