package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.zegreatrob.coupling.cli.ConfigFileSource
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon
import kotlin.time.Clock

private class Party : SuspendingCliktCommand() {
    init {
        context { valueSource = ConfigFileSource(readEnvvar) }
    }

    private val partyId by option()
        .convert { PartyId(it) }
        .required()

    private val env by option().default("production")
    override suspend fun run() {
        currentContext.findOrSetObject<PartyId>("partyId") { partyId }
        currentContext.findOrSetObject { PartyContext(partyId, env) }
    }
}

data class PartyContext(val partyId: PartyId?, val env: String)

fun party(cannon: ActionCannon<CouplingSdkDispatcher>?): SuspendingCliktCommand = Party()
    .subcommands(PartyList())
    .subcommands(PartyDetails(cannon))
    .subcommands(CurrentPairs(cannon))
    .subcommands(
        Contribution()
            .subcommands(SaveContribution(clock = Clock.System))
            .subcommands(BatchContribution()),
    )
