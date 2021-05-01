@file:JsModule("graphql")
@file:Suppress("unused")


package com.zegreatrob.coupling.server.external.graphql

import kotlin.js.Json

external class GraphQLSchema(config: Json)

external class GraphQLList(type: GraphQLType) : GraphQLType
external class GraphQLNonNull(type: GraphQLType) : GraphQLType
external class GraphQLObjectType(config: Json) : GraphQLType

external object GraphQLInt : GraphQLType
external object GraphQLBoolean : GraphQLType
external object GraphQLFloat : GraphQLType
external object GraphQLString : GraphQLType

external interface GraphQLType

