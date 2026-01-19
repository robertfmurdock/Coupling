package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import com.zegreatrob.coupling.cli.party.party
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.serialization.json.Json
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

val couplingHomeDirectory = "${getEnv("HOME")}/.coupling"
val configFilePath = "$couplingHomeDirectory/config.json"

fun getAccessToken() = getEnv("COUPLING_CLI_ACCESS_TOKEN")
    ?: readFileText(configFilePath)
        ?.let(Json.Default::parseToJsonElement)
        ?.let { it.jsonObject["accessToken"]?.jsonPrimitive?.contentOrNull }

fun getRefreshToken() = readFileText(configFilePath)
    ?.let(Json.Default::parseToJsonElement)
    ?.let { it.jsonObject["refreshToken"]?.jsonPrimitive?.contentOrNull }

fun getEnv() = readFileText(configFilePath)
    ?.let(Json.Default::parseToJsonElement)
    ?.let { it.jsonObject["env"]?.jsonPrimitive?.contentOrNull }

expect fun makeDirectory(couplingHomeDirectory: String)

expect fun writeDataToFile(configFilePath: String, text: String)
