package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlin.js.Json
import kotlin.js.json

val spinResolver: Resolver = dispatch(tribeCommand, { _, args ->
    ProposeNewPairsCommand(
        args.at<Array<Json>>("/input/players")?.map(Json::toPlayer)!!,
        args.at<Array<Json>>("/input/pins")?.map(Json::toPin)!!
    )
}, { json("result" to it.toJson()) })
