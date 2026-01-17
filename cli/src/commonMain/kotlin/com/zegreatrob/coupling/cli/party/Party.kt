package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Clock

private class Party : SuspendingCliktCommand() {
    private val partyId by option()
    private val env by option().default("production")
    override suspend fun run() {
        val partyId = partyId?.let(::PartyId)
            ?.also { currentContext.findOrSetObject<PartyId>("partyId") { it } }
        currentContext.findOrSetObject { PartyContext(partyId, env) }
    }
}

data class PartyContext(val partyId: PartyId?, val env: String)

fun party(scope: CoroutineScope, cannon: ActionCannon<CouplingSdkDispatcher>?): SuspendingCliktCommand = Party()
    .subcommands(PartyList())
    .subcommands(PartyDetails(cannon))
    .subcommands(
        Contribution()
            .subcommands(SaveContribution(clock = Clock.System))
            .subcommands(BatchContribution()),
    )
