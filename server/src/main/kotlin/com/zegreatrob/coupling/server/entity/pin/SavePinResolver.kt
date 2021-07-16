package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.TribeInput
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.Serializable

val savePinResolver = dispatch(tribeCommand, { _, input: SavePinInput -> SavePinCommand(input.toPin()) }, { true })

private fun SavePinInput.toPin() = Pin(
    id = pinId,
    name = name,
    icon = icon
)

@Serializable
data class SavePinInput(
    val pinId: String?,
    override val tribeId: String,
    val name: String,
    val icon: String
): TribeInput
