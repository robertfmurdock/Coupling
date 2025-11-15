@file:JsModule("@graphql-tools/schema")

package com.zegreatrob.coupling.server.external.graphql.tools.schema

import com.zegreatrob.coupling.server.external.graphql.GraphQLSchema
import kotlin.js.Json

external fun makeExecutableSchema(input: Json): GraphQLSchema

external fun mergeSchemas(input: Json): GraphQLSchema
