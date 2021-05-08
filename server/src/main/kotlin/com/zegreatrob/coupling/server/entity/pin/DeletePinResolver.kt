package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlin.js.Json

val deletePinResolver = dispatch(
    tribeCommand,
    { _, entity ->
        val input = entity["input"].unsafeCast<Json>()
        val pinId = input["pinId"].toString()
        DeletePinCommand(pinId)
    }, { true })
