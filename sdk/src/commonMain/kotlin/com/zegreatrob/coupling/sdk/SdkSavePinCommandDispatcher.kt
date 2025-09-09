package com.zegreatrob.coupling.sdk

import com.example.SavePinMutation
import com.example.type.SavePinInput
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkSavePinCommandDispatcher :
    SavePinCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SavePinCommand): VoidResult.Accepted {
        val (partyId, pin) = command
        apolloMutation(SavePinMutation(partyId.with(pin).savePinInput()))
        return VoidResult.Accepted
    }
}

private fun PartyElement<Pin>.savePinInput() = SavePinInput(
    partyId = partyId,
    pinId = element.id,
    icon = element.icon,
    name = element.name,
)
