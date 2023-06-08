package com.zegreatrob.coupling.cli.party

import com.benasher44.uuid.uuid4
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.Auth0Environment
import com.zegreatrob.coupling.cli.getAccessToken
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.sdk.KtorCouplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.coupling.sdk.gql.graphQuery
import kotlinx.coroutines.runBlocking
import java.net.URL

class List : CliktCommand() {
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
                    ?.map(Record<Party>::data)
                    ?.joinToString("\n") { "Party: id = ${it.id.value}, name = ${it.name}" }
                    .let { it ?: "" }
                    .let { echo(it) }
            }
        }
    }

    private fun Auth0Environment.audienceHost() = "https://${URL(audience).host}"
}