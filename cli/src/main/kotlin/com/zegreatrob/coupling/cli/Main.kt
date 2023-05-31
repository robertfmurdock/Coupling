package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.core.subcommands
import com.zegreatrob.coupling.cli.party.party
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

fun main(args: Array<String>) = Welcome()
    .subcommands(Login())
    .subcommands(
        party(),
    )
    .main(args)

val configFile = File("${System.getenv("HOME")}/.coupling/config.json")

fun getAccessToken() = configFile.readText()
    .let(Json.Default::parseToJsonElement)
    .let { it.jsonObject["accessToken"]?.jsonPrimitive?.content }
