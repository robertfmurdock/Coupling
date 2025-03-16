package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.json.GqlDeletePinInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery
import kotools.types.text.NotBlankString

interface SdkDeletePinCommandDispatcher :
    DeletePinCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePinCommand) = with(command) {
        (doQuery(Mutation.deletePin, deletePinInput(command.partyId, pinId), "deletePin", ::passThrough) ?: false)
            .voidResult()
    }
}

private fun passThrough(it: Boolean?): Boolean? = it
private fun deletePinInput(partyId: PartyId, pinId: NotBlankString) = GqlDeletePinInput(
    partyId = partyId.value,
    pinId = pinId,
)
