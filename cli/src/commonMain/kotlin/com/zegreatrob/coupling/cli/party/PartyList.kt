package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.SdkProvider
import com.zegreatrob.coupling.cli.gql.PartyListQuery
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain

internal fun formatPartyList(parties: List<String>): String = parties.joinToString("\n")

class PartyList(private val sdkProvider: SdkProvider) : SuspendingCliktCommand("list") {
    private val env by option().default("production")

    override suspend fun run() {
        withSdk(env = env, echo = ::echo, sdkProvider = sdkProvider) { sdk ->
            val output = sdk.fire(GqlQuery(PartyListQuery()))
                ?.partyList
                ?.map { it.partyDetails.toDomain() }
                ?.map { "Party: id = ${it.id.value}, name = ${it.name}" }
                ?.let(::formatPartyList)
                ?: ""
            echo(output)
        }
    }
}
