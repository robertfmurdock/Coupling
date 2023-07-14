package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.fire
import com.zegreatrob.coupling.json.DeletePinInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val deletePinResolver = dispatchAction(
    dispatcherFunc = { request, _: JsonNull, args: DeletePinInput ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId.value,
        )
    },
    commandFunc = { _, input -> input.toCommand() },
    fireFunc = ::fire,
    toSerializable = { true },
)

private fun DeletePinInput.toCommand() = DeletePinCommand(partyId, pinId)
