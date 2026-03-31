package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.gql.PartyListQuery
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.testmints.action.ActionCannon

class PartyList(private val cannon: ActionCannon<CouplingSdkDispatcher>?) : SuspendingCliktCommand("list") {
    private val env by option().default("production")
    override suspend fun run() {
        val handleResult: (PartyListQuery.Data?) -> Unit = { data ->
            data?.partyList
                ?.map { it.partyDetails.toDomain() }
                ?.joinToString("\n") { "Party: id = ${it.id.value}, name = ${it.name}" }
                .let { it ?: "" }
                .let { echo(it) }
        }

        cannon?.let { actionCannon ->
            handleResult(actionCannon.fire(GqlQuery(PartyListQuery())))
        } ?: withSdk(env, ::echo) { sdk ->
            handleResult(sdk.fire(GqlQuery(PartyListQuery())))
        }
    }
}
