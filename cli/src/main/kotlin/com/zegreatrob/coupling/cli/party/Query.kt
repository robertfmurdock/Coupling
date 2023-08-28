package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.sdk.gql.GraphQuery
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

private class Query : CliktCommand() {
    private val env by option().default("production")
    val query by argument()
    val variableJson by argument()
    override fun run() = runBlocking {
        val variables = Json.parseToJsonElement(variableJson).jsonObject
        withSdk(env, ::echo) { sdk ->
            echo(sdk.fire(GraphQuery(query, variables))?.raw ?: "No response.")
        }
    }
}

fun query(): CliktCommand = Query()
