package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.tribe.TribeType
import com.zegreatrob.coupling.server.entity.tribe.deleteTribeResolver
import com.zegreatrob.coupling.server.entity.tribe.tribeListResolve
import com.zegreatrob.coupling.server.external.graphql.*
import kotlin.js.json

private val entityWithId: Resolver = { _, args, _ ->
    json("id" to args["id"])
}

fun couplingSchema() = GraphQLSchema(
    json(
        "query" to objectType(
            name = "Query",
            fields = arrayOf(
                field("tribeList", GraphQLList(TribeType), tribeListResolve),
                field("tribeData", TribeDataType, entityWithId, json("id" to field(GraphQLString)))
            )
        ),
        "mutation" to objectType(
            name = "Mutation",
            fields = arrayOf(
                field("deleteTribe", GraphQLBoolean, deleteTribeResolver, json("tribeId" to field(GraphQLString)))
            ),
        )
    )
)
