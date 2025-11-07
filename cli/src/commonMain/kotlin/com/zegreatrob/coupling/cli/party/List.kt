package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.cliScope
import com.zegreatrob.coupling.cli.gql.PartyListQuery
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain

class List : CliktCommand() {
    private val env by option().default("production")
    override fun run() {
        withSdk(cliScope, env, ::echo) { sdk ->
            sdk.fire(GqlQuery(PartyListQuery()))
                ?.partyList
                ?.map { it.partyDetails.toDomain() }
                ?.joinToString("\n") { "Party: id = ${it.id.value}, name = ${it.name}" }
                .let { it ?: "" }
                .let { echo(it) }
        }
    }
}
