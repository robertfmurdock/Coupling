@file:JsModule("graphql")
@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.graphql

import kotlin.js.Json

external class GraphQLSchema(config: Json)

external fun buildSchema(schema: String): GraphQLSchema
external fun printSchema(schema: GraphQLSchema): String

external interface GraphQLType
