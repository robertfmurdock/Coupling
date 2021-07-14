package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.json

val spinResolver: Resolver = dispatch(tribeCommand, { _, args ->
    val (_, players, pins) = couplingJsonFormat.decodeFromDynamic<SpinInput>(args.at("input"))
    ProposeNewPairsCommand(
        players.map(JsonPlayerData::toModel),
        pins.map(JsonPinData::toModel),
    )
}, { json("result" to it.toJson()) })
