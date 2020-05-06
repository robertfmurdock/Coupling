package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.tribe.TribeType
import com.zegreatrob.coupling.server.entity.tribe.tribeListResolve
import com.zegreatrob.coupling.server.external.graphql.*
import kotlin.js.json

private val entityWithId: Resolver = { _, args, _ ->
    json("id" to args["id"])
}

fun couplingSchema(): GraphQLSchema {
    return GraphQLSchema(
        json(
            "query" to objectType(
                name = "RootQueryType",
                fields = arrayOf(
                    field("tribeList", GraphQLList(TribeType), tribeListResolve),
                    field("tribeData",
                        TribeDataType, args = json("id" to field(GraphQLString)), resolve = entityWithId
                    )
                )
            )
        )
    )
}
