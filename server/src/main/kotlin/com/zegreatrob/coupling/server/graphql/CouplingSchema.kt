package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.pairassignment.pairAssignmentListResolve
import com.zegreatrob.coupling.server.entity.pairassignment.spinResolver
import com.zegreatrob.coupling.server.entity.pin.pinListResolve
import com.zegreatrob.coupling.server.entity.player.playerListResolve
import com.zegreatrob.coupling.server.entity.player.retiredPlayerListResolve
import com.zegreatrob.coupling.server.entity.tribe.deleteTribeResolver
import com.zegreatrob.coupling.server.entity.tribe.tribeListResolve
import com.zegreatrob.coupling.server.entity.tribe.tribeResolve
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.external.graphql_tools.makeExecutableSchema
import kotlin.js.json

private val entityWithId: Resolver = { _, args, _ ->
    println("entity with id ${JSON.stringify(args)}")
    json("id" to args["id"])
}

fun couplingSchema() = makeExecutableSchema(
    json(
        "typeDefs" to "${js("require('schema.graphql')").default}",
        "resolvers" to couplingResolvers()
    )
)

fun couplingResolvers() = json(
    "Query" to json(
        "tribeList" to tribeListResolve,
        "tribeData" to entityWithId,
    ),
    "Mutation" to json(
        "deleteTribe" to deleteTribeResolver,
        "spin" to spinResolver,
    ),
    "TribeData" to json(
        "tribe" to tribeResolve,
        "pinList" to pinListResolve,
        "playerList" to playerListResolve,
        "retiredPlayers" to retiredPlayerListResolve,
        "pairAssignmentDocumentList" to pairAssignmentListResolve
    ),
)
