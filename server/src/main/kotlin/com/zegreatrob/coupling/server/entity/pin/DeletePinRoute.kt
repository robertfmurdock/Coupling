package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlin.js.Json

val deletePinResolver = dispatch(
    command,
    { _, entity ->
        val input = entity["input"].unsafeCast<Json>()
        val tribeId = TribeId(input["tribeId"].toString())
        val pinId = input["pinId"].toString()
        DeletePinCommand(tribeId, pinId)
    }, { true })

