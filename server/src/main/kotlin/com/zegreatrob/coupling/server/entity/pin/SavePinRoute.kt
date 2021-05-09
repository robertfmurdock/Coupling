package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.defaultPin
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlin.js.Json

val savePinResolver = dispatch(
    tribeCommand,
    { _, args -> args.savePinInput().toPin().let(::SavePinCommand) },
    { true }
)

private fun Json.savePinInput() = this["input"].unsafeCast<Json>()

private fun Json.toPin() = Pin(
    id = this["pinId"]?.toString(),
    name = this["name"]?.toString() ?: defaultPin.name,
    icon = this["icon"]?.toString() ?: defaultPin.icon
)
