package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.entity.tribe.TribeType
import com.zegreatrob.coupling.server.entity.tribe.performTribeListQueryGQL
import com.zegreatrob.coupling.server.external.graphql.*
import kotlin.js.json

private val entityWithId: Resolver = { _, args, _ ->
    json("id" to args["id"])
}

fun couplingSchema() = GraphQLSchema(
    json(
        "query" to objectType(
            name = "RootQueryType",
            fields = arrayOf(
                field("tribeList", GraphQLList(TribeType)) { _, _ -> performTribeListQueryGQL() },
                field("tribeData",
                    TribeDataType, args = json("id" to field(GraphQLString)), resolve = entityWithId)
            )
        )
    )
)
