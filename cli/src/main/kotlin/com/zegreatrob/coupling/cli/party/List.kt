package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.sdk.gql.graphQuery

class List : CliktCommand() {
    private val env by option().default("production")
    override fun run() {
        withSdk(env, ::echo) { sdk ->
            sdk.fire(graphQuery { partyList { details() } })
                ?.partyList
                ?.mapNotNull { it.details?.data }
                ?.joinToString("\n") { "Party: id = ${it.id.value}, name = ${it.name}" }
                .let { it ?: "" }
                .let { echo(it) }
        }
    }
}
