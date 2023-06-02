package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.SavePinInput
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePinResolver = dispatch(
    dispatcherFunc = { request, _: JsonNull, args -> authorizedDispatcher(request = request, partyId = args.partyId.value) },
    queryFunc = { _, input: SavePinInput -> SavePinCommand(input.toPin()) },
    toSerializable = { true },
)

private fun SavePinInput.toPin() = Pin(
    id = pinId,
    name = name,
    icon = icon,
)
