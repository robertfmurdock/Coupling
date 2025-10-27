@file:JsModule("graphql")
@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.graphql

import kotlinx.js.JsPlainObject
import kotlin.js.Json

external class GraphQLSchema(config: Json)

external class GraphQLScalarType<T>(config: GraphQLScalarTypeConfig<T>)

external object GraphQLString

external interface GraphQLScalarTypeConfig<T> {
    var name: String
    var description: String?
    var serialize: (Any) -> T
    var parseValue: ((Any) -> T)?
    var parseLiteral: ((AST) -> T)?
}

@JsPlainObject
external interface AST {
    val kind: Any
    val value: dynamic
}

external fun buildSchema(schema: String): GraphQLSchema
external fun printSchema(schema: GraphQLSchema): String

external interface GraphQLType
