package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlin.js.Json
import kotlin.js.json

val spinResolver: Resolver = dispatch(command, { _, args ->
    val input = args["input"].unsafeCast<Json>()
    val tribeId = input["tribeId"].unsafeCast<String>().let(::TribeId)
    val players = input["players"].unsafeCast<Array<Json>>().map(Json::toPlayer)
    val pins = input["pins"].unsafeCast<Array<Json>>().map(Json::toPin)
    ProposeNewPairsCommand(tribeId, players, pins)
}, { json("result" to it.toJson()) })
