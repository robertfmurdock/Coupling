package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.pairassignment.*
import com.zegreatrob.coupling.server.entity.pin.deletePinResolver
import com.zegreatrob.coupling.server.entity.pin.pinListResolve
import com.zegreatrob.coupling.server.entity.pin.savePinResolver
import com.zegreatrob.coupling.server.entity.player.deletePlayerResolver
import com.zegreatrob.coupling.server.entity.player.playerListResolve
import com.zegreatrob.coupling.server.entity.player.retiredPlayerListResolve
import com.zegreatrob.coupling.server.entity.player.savePlayerResolver
import com.zegreatrob.coupling.server.entity.tribe.deleteTribeResolver
import com.zegreatrob.coupling.server.entity.tribe.saveTribeResolver
import com.zegreatrob.coupling.server.entity.tribe.tribeListResolve
import com.zegreatrob.coupling.server.entity.tribe.tribeResolve
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.external.graphql_tools.makeExecutableSchema
import kotlin.js.json

private val entityWithId: Resolver = { _, args, _ -> json("id" to args["id"]) }

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
        "spin" to spinResolver,
        "saveTribe" to saveTribeResolver,
        "deleteTribe" to deleteTribeResolver,
        "savePin" to savePinResolver,
        "deletePin" to deletePinResolver,
        "savePlayer" to savePlayerResolver,
        "deletePlayer" to deletePlayerResolver,
        "savePairAssignments" to savePairsResolver,
        "deletePairAssignments" to deletePairsResolver,
    ),
    "TribeData" to json(
        "tribe" to tribeResolve,
        "pinList" to pinListResolve,
        "playerList" to playerListResolve,
        "retiredPlayers" to retiredPlayerListResolve,
        "pairAssignmentDocumentList" to pairAssignmentListResolve,
        "currentPairAssignmentDocument" to currentPairAssignmentResolve,
    ),
)
