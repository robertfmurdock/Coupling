package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.express.route.ExpressDispatchers.command
import com.zegreatrob.coupling.server.express.route.dispatch
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.jsonBody
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import kotlin.js.Json

val spinRoute = dispatch(command, Request::command, PairAssignmentDocument::toJson)

private fun Request.command() = ProposeNewPairsCommand(
    tribeId(),
    jsonBody()["players"].unsafeCast<Array<Json>>().map(Json::toPlayer),
    jsonBody()["pins"].unsafeCast<Array<Json>>().map(Json::toPin)
)

val spinResolver: Resolver = com.zegreatrob.coupling.server.graphql.dispatch(DispatcherProviders.command, { _, args ->
    val input = args["input"].unsafeCast<Json>()
    println("spinput ${JSON.stringify(input)}")

    ProposeNewPairsCommand(
        input["tribeId"].unsafeCast<String>().let(::TribeId),
        input["players"].unsafeCast<Array<Json>>().map(Json::toPlayer),
        input["pins"].unsafeCast<Array<Json>>().map(Json::toPin)
    )
}, PairAssignmentDocument::toJson)
