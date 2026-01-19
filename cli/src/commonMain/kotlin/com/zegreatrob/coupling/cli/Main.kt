package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import com.zegreatrob.coupling.cli.party.party
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend fun main(args: Array<String>) {
    cli()
        .main(platformArgCorrection(args))
}

fun cli(cannon: ActionCannon<CouplingSdkDispatcher>? = null): CouplingCli = CouplingCli()
    .subcommands(Login())
    .subcommands(ConfigCommand())
    .subcommands(party(cannon))

expect fun platformArgCorrection(args: Array<String>): Array<String>

expect fun getEnv(variableName: String): String?
expect fun readFileText(filePath: String): String?

suspend fun getAccessToken() = getEnv("COUPLING_CLI_ACCESS_TOKEN")
    ?: loadTokens()?.accessToken()

fun JsonElement.accessToken(): String? = jsonObject["accessToken"]?.jsonPrimitive?.contentOrNull

suspend fun loadTokens(): JsonElement? = getSecureData("tokens")
    ?.let(Json.Default::parseToJsonElement)

fun JsonElement.refreshToken(): String? = jsonObject["refreshToken"]?.jsonPrimitive?.contentOrNull

fun JsonElement.env(): String? = jsonObject["env"]?.jsonPrimitive?.contentOrNull

expect fun makeDirectory(couplingHomeDirectory: String)

expect fun writeDataToFile(configFilePath: String, text: String)
