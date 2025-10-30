@file:OptIn(DelicateCoroutinesApi::class)

package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.cliScope
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.sdk.gql.RawGraphQuery
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

private class Query : CliktCommand() {
    private val env by option().default("production")
    val query by argument()
    val variableJson by argument()
    override fun run() {
        cliScope.launch {
            val variables = Json.parseToJsonElement(variableJson).jsonObject
            withSdk(cliScope, env, ::echo) { sdk ->
                echo(sdk.fire(RawGraphQuery(query, variables))?.raw ?: "No response.")
            }
        }
    }
}

fun query(): CliktCommand = Query()
