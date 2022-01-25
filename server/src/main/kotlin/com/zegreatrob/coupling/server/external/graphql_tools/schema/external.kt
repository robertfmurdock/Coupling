@file:JsModule("@graphql-tools/schema")
@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.graphql_tools.schema

import com.zegreatrob.coupling.server.external.graphql.GraphQLSchema
import kotlin.js.Json

external fun makeExecutableSchema(input: Json): GraphQLSchema
