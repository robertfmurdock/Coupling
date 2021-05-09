package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.pairassignment.deletePairsRoute
import com.zegreatrob.coupling.server.entity.pairassignment.pairAssignmentListResolve
import com.zegreatrob.coupling.server.entity.pairassignment.savePairsRoute
import com.zegreatrob.coupling.server.entity.pairassignment.spinResolver
import com.zegreatrob.coupling.server.entity.pin.deletePinResolver
import com.zegreatrob.coupling.server.entity.pin.pinListResolve
import com.zegreatrob.coupling.server.entity.pin.savePinResolver
import com.zegreatrob.coupling.server.entity.player.deletePlayerResolver
import com.zegreatrob.coupling.server.entity.player.playerListResolve
import com.zegreatrob.coupling.server.entity.player.retiredPlayerListResolve
import com.zegreatrob.coupling.server.entity.player.savePlayerRoute
import com.zegreatrob.coupling.server.entity.tribe.deleteTribeResolver
import com.zegreatrob.coupling.server.entity.tribe.saveTribeResolver
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
        "saveTribe" to saveTribeResolver,
        "deleteTribe" to deleteTribeResolver,
        "savePin" to savePinResolver,
        "savePairAssignments" to savePairsRoute,
        "deletePin" to deletePinResolver,
        "savePlayer" to savePlayerRoute,
        "deletePlayer" to deletePlayerResolver,
        "deletePairAssignments" to deletePairsRoute,
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
