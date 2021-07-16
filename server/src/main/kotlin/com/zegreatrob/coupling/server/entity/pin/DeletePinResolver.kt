package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.TribeInput
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.Serializable

val deletePinResolver = dispatch(tribeCommand, { _, args: DeletePinInput -> DeletePinCommand(args.pinId) }, { true })

@Serializable
data class DeletePinInput(
    override val tribeId: String,
    val pinId: String
): TribeInput
