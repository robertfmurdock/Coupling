package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.entity.buildResolver
import com.zegreatrob.coupling.server.entity.tribe.performTribeListQueryGQL
import com.zegreatrob.coupling.server.entity.tribe.performTribeQueryGQL
import com.zegreatrob.coupling.server.entity.verifyAuth
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.graphql.*
import kotlin.js.Json
import kotlin.js.json


val TribeType = objectType(
    name = "Tribe",
    description = "The people you couple with!",
    fields = arrayOf(
        field("id", GraphQLNonNull(GraphQLString)),
        field("name", GraphQLString),
        field("email", GraphQLString),
        field("pairingRule", GraphQLString),
        field("defaultBadgeName", GraphQLString),
        field("alternateBadgeName", GraphQLString),
        field("badgesEnabled", GraphQLBoolean),
        field("callSignsEnabled", GraphQLBoolean),
        field("animationsEnabled", GraphQLBoolean),
        field("animationSpeed", GraphQLFloat),
        field("modifyingUserEmail", GraphQLString),
        field("timestamp", GraphQLString),
        field("isDeleted", GraphQLBoolean)
    )
)

private fun objectType(name: String, description: String = "", fields: Array<Pair<String, Any?>>) = GraphQLObjectType(
    json(
        "name" to name,
        "description" to description,
        "fields" to json(*fields)
    )
)

private fun field(name: String, type: GraphQLType, args: Json? = null, resolve: Resolver? = null) =
    name to json("type" to type)
        .let { if (args == null) it else it.add(json("args" to args)) }
        .let { if (resolve == null) it else it.add(json("resolve" to resolve)) }

val PinType = objectType(
    name = "Pin",
    description = "Something to put on your shirt!!",
    fields = arrayOf(
        field("_id", GraphQLNonNull(GraphQLString)),
        field("icon", GraphQLString),
        field("name", GraphQLString),
        field("modifyingUserEmail", GraphQLString),
        field("timestamp", GraphQLString),
        field("isDeleted", GraphQLBoolean)
    )
)

val PlayerType = objectType(
    name = "Player",
    description = "Weirdos who want to couple",
    fields = arrayOf(
        field("_id", GraphQLNonNull(GraphQLString)),
        field("name", GraphQLString),
        field("email", GraphQLString),
        field("badge", GraphQLString),
        field("callSignAdjective", GraphQLString),
        field("callSignNoun", GraphQLString),
        field("imageURL", GraphQLString),
        field("modifyingUserEmail", GraphQLString),
        field("timestamp", GraphQLString),
        field("isDeleted", GraphQLBoolean)
    )
)

val PinnedPlayerType = objectType(
    name = "PinnedPlayer",
    description = "",
    fields = arrayOf(
        field("_id", GraphQLString),
        field("name", GraphQLString),
        field("email", GraphQLString),
        field("badge", GraphQLString),
        field("callSignAdjective", GraphQLString),
        field("callSignNoun", GraphQLString),
        field("imageURL", GraphQLString),
        field("pins", GraphQLList(PinType))
    )
)

val PinnedPairType = objectType(
    name = "PinnedPair",
    fields = arrayOf(
        field("players", GraphQLList(PinnedPlayerType)),
        field("pins", GraphQLList(PinType))
    )
)

val PairAssignmentDocumentType = objectType(
    name = "PairAssignmentDocument",
    description = "Assignments!",
    fields = arrayOf(
        field("_id", GraphQLNonNull(GraphQLString)),
        field("date", GraphQLNonNull(GraphQLString)),
        field("pairs", GraphQLList(PinnedPairType)),
        field("modifyingUserEmail", GraphQLString),
        field("timestamp", GraphQLString),
        field("isDeleted", GraphQLBoolean)
    )
)

val TribeDataType = objectType(
    name = "TribeData",
    description = "Everything you wanted to know about a tribe but never asked.",
    fields = arrayOf(
        field("id", GraphQLNonNull(GraphQLString)),
        field("tribe", TribeType) { entity, _ -> performTribeQueryGQL(entity["id"].toString()) },
        field("pinList", GraphQLList(PinType), verifyAuth { performPinListQueryGQL() }),
        field("playerList", GraphQLList(PlayerType), verifyAuth { performPlayerListQueryGQL() }),
        field(
            "pairAssignmentDocumentList",
            GraphQLList(PairAssignmentDocumentType),
            verifyAuth { performPairAssignmentListQueryGQL() }
        )
    )
)

fun couplingSchema() = GraphQLSchema(
    json(
        "query" to objectType(
            name = "RootQueryType",
            fields = arrayOf(
                field("tribeList", GraphQLList(TribeType)) { _, _ -> performTribeListQueryGQL() },
                field("tribeData", TribeDataType, args = json("id" to field(GraphQLString))) { _, args, _ ->
                    json("id" to args["id"])
                }
            )
        )
    )
)

private fun field(name: String, type: GraphQLType, commandResolve: suspend CommandDispatcher.(Json, Json) -> Any?) =
    field(name, type, resolve = buildResolver(commandResolve))

typealias Resolver = (Json, Json, Request) -> Any?

private fun field(type: GraphQLType, args: Json? = null, resolve: Resolver? = null) = json("type" to type)
    .let { if (args == null) it else it.add(json("args" to args)) }
    .let { if (resolve == null) it else it.add(json("resolve" to resolve)) }
