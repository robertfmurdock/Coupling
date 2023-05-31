package com.zegreatrob.coupling.cli

import com.benasher44.uuid.uuid4
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.sdk.KtorCouplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.coupling.sdk.gql.graphQuery
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.URL

fun main(args: Array<String>) = Welcome()
    .subcommands(Login())
    .subcommands(
        Party()
            .subcommands(PartyList()),
    )
    .main(args)

class Party : CliktCommand() {
    override fun run() {
    }
}

class PartyList : CliktCommand() {
    private val env by option().default("production")
    override fun run() {
        val environment = Auth0Environment.map[env]
        if (environment == null) {
            echo("Environment not found.")
        } else {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                echo("You are not currently logged in.")
                return
            }

            val sdk = KtorCouplingSdk(
                { accessToken },
                traceId = uuid4(),
                defaultClient(environment.audienceHost() to ""),
            )
            runBlocking {
                sdk.perform(graphQuery { partyList() })
                    ?.partyList
                    ?.map(Record<com.zegreatrob.coupling.model.party.Party>::data)
                    ?.joinToString("\n") { "Party: id = ${it.id.value}, name = ${it.name}" }
                    .let { it ?: "" }
                    .let { echo(it) }
            }
        }
    }

    private fun Auth0Environment.audienceHost() = "https://${URL(audience).host}"
}

val configFile = File("${System.getenv("HOME")}/.coupling/config.json")

fun getAccessToken() = configFile.readText()
    .let(Json.Default::parseToJsonElement)
    .let { it.jsonObject["accessToken"]?.jsonPrimitive?.content }
