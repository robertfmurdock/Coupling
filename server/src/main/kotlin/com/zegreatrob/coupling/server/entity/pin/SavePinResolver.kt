package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.defaultPin
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlin.js.Json

val savePinResolver = dispatch(tribeCommand, { _, args -> args.toPin().let(::SavePinCommand) }, { true })

private fun Json.toPin() = Pin(
    id = at("/input/pinId"),
    name = at("/input/name") ?: defaultPin.name,
    icon = at("/input/icon") ?: defaultPin.icon
)
