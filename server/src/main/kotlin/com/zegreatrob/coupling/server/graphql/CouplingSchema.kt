package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.boost.boostResolver
import com.zegreatrob.coupling.server.entity.boost.deleteBoostResolver
import com.zegreatrob.coupling.server.entity.boost.saveBoostResolver
import com.zegreatrob.coupling.server.entity.pairassignment.currentPairAssignmentResolve
import com.zegreatrob.coupling.server.entity.pairassignment.deletePairsResolver
import com.zegreatrob.coupling.server.entity.pairassignment.pairAssignmentListResolve
import com.zegreatrob.coupling.server.entity.pairassignment.savePairsResolver
import com.zegreatrob.coupling.server.entity.pairassignment.spinResolver
import com.zegreatrob.coupling.server.entity.party.deletePartyResolver
import com.zegreatrob.coupling.server.entity.party.partyListResolve
import com.zegreatrob.coupling.server.entity.party.partyResolve
import com.zegreatrob.coupling.server.entity.party.savePartyResolver
import com.zegreatrob.coupling.server.entity.pin.deletePinResolver
import com.zegreatrob.coupling.server.entity.pin.pinListResolve
import com.zegreatrob.coupling.server.entity.pin.savePinResolver
import com.zegreatrob.coupling.server.entity.player.deletePlayerResolver
import com.zegreatrob.coupling.server.entity.player.playerListResolve
import com.zegreatrob.coupling.server.entity.player.retiredPlayerListResolve
import com.zegreatrob.coupling.server.entity.player.savePlayerResolver
import com.zegreatrob.coupling.server.entity.user.userResolve
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.graphql.GraphQLSchema
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.external.graphql.tools.schema.makeExecutableSchema
import com.zegreatrob.coupling.server.external.graphql.tools.schema.mergeSchemas
import kotlin.js.json

private val entityWithId: Resolver = { _, args, _, _ -> json("id" to args["id"]) }

fun couplingSchema() = makeExecutableSchema(
    json(
        "typeDefs" to "${js("require('schema.graphql')").default}",
        "resolvers" to couplingResolvers(),
    ),
)

fun prereleaseSchema() = makeExecutableSchema(
    json(
        "typeDefs" to "${js("require('prerelease-schema.graphql')").default}",
        "resolvers" to prereleaseResolvers(),
    ),
)

private fun prereleaseResolvers() = json(
    "Mutation" to json(
        "saveBoost" to saveBoostResolver,
        "deleteBoost" to deleteBoostResolver,
    ),
    "User" to json(
        "boost" to boostResolver,
    ),
)

fun unifiedSchema() = addPrereleaseSchema(couplingSchema())

private fun addPrereleaseSchema(standardSchema: GraphQLSchema) = if (!Config.prereleaseMode) {
    standardSchema
} else {
    try {
        mergeSchemas(json("schemas" to arrayOf(standardSchema, prereleaseSchema())))
    } catch (anything: Throwable) {
        println("error $anything")
        throw anything
    }
}

fun couplingResolvers() = json(
    "Query" to json(
        "user" to userResolve,
        "partyList" to partyListResolve,
        "partyData" to entityWithId,
    ),
    "Mutation" to json(
        "spin" to spinResolver,
        "saveParty" to savePartyResolver,
        "deleteParty" to deletePartyResolver,
        "savePin" to savePinResolver,
        "deletePin" to deletePinResolver,
        "savePlayer" to savePlayerResolver,
        "deletePlayer" to deletePlayerResolver,
        "savePairAssignments" to savePairsResolver,
        "deletePairAssignments" to deletePairsResolver,
    ),
    "PartyData" to json(
        "party" to partyResolve,
        "pinList" to pinListResolve,
        "playerList" to playerListResolve,
        "retiredPlayers" to retiredPlayerListResolve,
        "pairAssignmentDocumentList" to pairAssignmentListResolve,
        "currentPairAssignmentDocument" to currentPairAssignmentResolve,
    ),
)
