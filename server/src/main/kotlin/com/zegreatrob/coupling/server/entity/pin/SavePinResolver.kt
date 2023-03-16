package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.SavePinInput
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val savePinResolver = dispatch(partyCommand, { _, input: SavePinInput -> SavePinCommand(input.toPin()) }, { true })

private fun SavePinInput.toPin() = Pin(
    id = pinId,
    name = name,
    icon = icon,
)
