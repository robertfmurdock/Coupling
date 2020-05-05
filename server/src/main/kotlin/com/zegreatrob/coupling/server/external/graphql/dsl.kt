package com.zegreatrob.coupling.server.external.graphql

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.entity.buildResolver
import com.zegreatrob.coupling.server.external.express.Request
import kotlin.js.Json
import kotlin.js.json

fun objectType(name: String, description: String = "", fields: Array<Pair<String, Any?>>) =
    GraphQLObjectType(
        json(
            "name" to name,
            "description" to description,
            "fields" to json(*fields)
        )
    )

fun field(name: String, type: GraphQLType, args: Json? = null, resolve: Resolver? = null) =
    name to json("type" to type)
        .let { if (args == null) it else it.add(json("args" to args)) }
        .let { if (resolve == null) it else it.add(json("resolve" to resolve)) }

fun field(name: String, type: GraphQLType, commandResolve: suspend CommandDispatcher.(Json, Json) -> Any?) =
    field(name, type, resolve = buildResolver(commandResolve))

typealias Resolver = (Json, Json, Request) -> Any?

fun field(type: GraphQLType, args: Json? = null, resolve: Resolver? = null) = json("type" to type)
    .let { if (args == null) it else it.add(json("args" to args)) }
    .let { if (resolve == null) it else it.add(json("resolve" to resolve)) }