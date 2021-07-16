package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.Serializable

val spinResolver: Resolver = dispatch(
    tribeCommand,
    { _, args: SpinInput ->
        val (_, players, pins) = args
        ProposeNewPairsCommand(
            players.map(JsonPlayerData::toModel),
            pins.map(JsonPinData::toModel),
        )
    },
    { Return(it.toSerializable()) }
)

@Serializable
data class Return(val result: JsonPairAssignmentDocument)