package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSavePinCommandDispatcher : SavePinCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: SavePinCommand): SuccessfulResult<Unit> {
        val (partyId, pin) = command
        doQuery(Mutation.savePin, partyId.with(pin).savePinInput())
        return Unit.successResult()
    }
}
private fun PartyElement<Pin>.savePinInput() =
    mapOf(
        "partyId" to partyId.value,
        "pinId" to element.id,
        "icon" to element.icon,
        "name" to element.name,
    )
