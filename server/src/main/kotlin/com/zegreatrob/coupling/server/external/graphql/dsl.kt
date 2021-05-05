package com.zegreatrob.coupling.server.external.graphql

import com.zegreatrob.coupling.server.external.express.Request
import kotlin.js.Json
import kotlin.js.json

fun objectType(
    name: String,
    description: String = "",
    fields: Array<Pair<String, Any?>>,
    args: Array<Pair<String, Any?>> = emptyArray()
) = GraphQLObjectType(
    json(
        "name" to name,
        "description" to description,
        "fields" to json(*fields),
    ).add(
        json("args" to json(*args))
    )
)

fun field(type: GraphQLType) = json("type" to type)

fun field(name: String, type: GraphQLType, resolve: Resolver? = null, args: Json? = null) =
    name to json("type" to type)
        .let { if (args == null) it else it.add(json("args" to args)) }
        .let { if (resolve == null) it else it.add(json("resolve" to resolve)) }

typealias Resolver = (Json, Json, Request) -> Any?


