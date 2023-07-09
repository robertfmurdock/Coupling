package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.json.SavePinInput
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePinResolver = dispatch(
    dispatcherFunc = { request, _: JsonNull, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId.value,
        )
    },
    queryFunc = { _, input: SavePinInput -> input.toCommand() },
    toSerializable = { true },
)

private fun SavePinInput.toCommand() = SavePinCommand(partyId, toPin())

private fun SavePinInput.toPin() = Pin(
    id = pinId,
    name = name,
    icon = icon,
)
