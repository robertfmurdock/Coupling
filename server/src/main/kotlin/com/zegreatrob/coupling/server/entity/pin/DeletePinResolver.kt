package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic

val deletePinResolver = dispatch(
    tribeCommand,
    { _, args ->
        couplingJsonFormat.decodeFromDynamic<DeletePinInput>(args.at<String>("/input"))
            .let { it.pinId }
            .let(::DeletePinCommand)
    },
    { true }
)

@Serializable
data class DeletePinInput(
    val tribeId: String,
    val pinId: String
)
