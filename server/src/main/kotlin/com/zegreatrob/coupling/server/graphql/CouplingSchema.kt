package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.graphql.GraphQLSchema
import com.zegreatrob.coupling.server.external.graphql.tools.schema.makeExecutableSchema
import com.zegreatrob.coupling.server.external.graphql.tools.schema.mergeSchemas
import kotlin.js.json

fun couplingSchema() = makeExecutableSchema(
    json(
        "typeDefs" to "${js("require('schema.graphql')").default}",
        "resolvers" to couplingResolvers(),
    ),
)

fun prereleaseSchema() = makeExecutableSchema(
    json(
        "typeDefs" to "${js("require('prerelease-schema.graphql')").default}",
        "resolvers" to prereleaseResolvers(),
    ),
)

fun unifiedSchema() = addPrereleaseSchema(couplingSchema())

private fun addPrereleaseSchema(standardSchema: GraphQLSchema) = if (!Config.prereleaseMode) {
    standardSchema
} else {
    try {
        mergeSchemas(json("schemas" to arrayOf(standardSchema, prereleaseSchema())))
    } catch (anything: Throwable) {
        println("error $anything")
        throw anything
    }
}
