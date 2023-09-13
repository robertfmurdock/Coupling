package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.core.subcommands
import com.zegreatrob.coupling.cli.party.party
import com.zegreatrob.coupling.cli.party.query
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val cliScope = CliScope()

suspend fun main(args: Array<String>) {
    Welcome()
        .subcommands(Login())
        .subcommands(party())
        .subcommands(query())
        .main(platformArgCorrection(args))
    cliScope.joinAll()
}

expect fun platformArgCorrection(args: Array<String>): Array<String>

expect fun getEnv(variableName: String): String?
expect fun readFileText(filePath: String): String

val couplingHomeDirectory = "${getEnv("HOME")}/.coupling"
val configFilePath = "$couplingHomeDirectory/config.json"

fun getAccessToken() = getEnv("COUPLING_CLI_ACCESS_TOKEN")
    ?: readFileText(configFilePath)
        .let(Json.Default::parseToJsonElement)
        .let { it.jsonObject["accessToken"]?.jsonPrimitive?.content }

expect fun makeDirectory(couplingHomeDirectory: String)

expect fun writeDataToFile(configFilePath: String, text: String)
