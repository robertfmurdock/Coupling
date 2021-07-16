package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json

val savePinResolver = dispatch(tribeCommand, { _, args -> args.toPin().let(::SavePinCommand) }, { true })

private fun Json.toPin() = couplingJsonFormat.decodeFromDynamic<SavePinInput>(at("/input")).let {
    Pin(
        id = it.pinId,
        name = it.name,
        icon = it.icon
    )
}

@Serializable
data class SavePinInput(
    val pinId: String?,
    val tribeId: String,
    val name: String,
    val icon: String
)
