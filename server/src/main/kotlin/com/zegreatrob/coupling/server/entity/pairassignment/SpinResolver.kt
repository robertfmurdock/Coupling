package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.JsonPairAssignmentDocument
import com.zegreatrob.coupling.json.JsonPinData
import com.zegreatrob.coupling.json.JsonPlayerData
import com.zegreatrob.coupling.json.SpinInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.Serializable

val spinResolver: Resolver = dispatch(
    partyCommand,
    { _, args: SpinInput ->
        val (_, players, pins) = args
        ProposeNewPairsCommand(
            players.map(JsonPlayerData::toModel),
            pins.map(JsonPinData::toModel)
        )
    },
    { Return(it.toSerializable()) }
)

@Serializable
data class Return(val result: JsonPairAssignmentDocument)
