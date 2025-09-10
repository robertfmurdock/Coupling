package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.DeletePinMutation
import com.zegreatrob.coupling.sdk.schema.type.DeletePinInput

interface SdkDeletePinCommandDispatcher :
    DeletePinCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePinCommand) = with(command) {
        (DeletePinMutation(deletePinInput(command.partyId, pinId)).execute().data?.deletePin ?: false)
            .voidResult()
    }
}

private fun deletePinInput(partyId: PartyId, pinId: PinId) = DeletePinInput(
    partyId = partyId,
    pinId = pinId,
)
