package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.ConfigFileSource
import com.zegreatrob.coupling.cli.SdkProvider
import com.zegreatrob.coupling.model.party.PartyId
import kotlin.time.Clock

private class Party : SuspendingCliktCommand() {
    init {
        context { valueSource = ConfigFileSource(readEnvvar) }
    }

    private val partyId by option()
        .convert { PartyId(it) }

    private val env by option().default("production")
    override suspend fun run() {
        val commandName = currentContext.invokedSubcommand?.commandName
        if (partyId == null && commandName != "list") {
            throw UsageError("missing option --party-id")
        }
        partyId?.let {
            currentContext.findOrSetObject<PartyId>("partyId") { it }
        }
        currentContext.findOrSetObject { PartyContext(partyId, env) }
    }
}

data class PartyContext(val partyId: PartyId?, val env: String)

fun party(sdkProvider: SdkProvider): SuspendingCliktCommand = Party()
    .subcommands(PartyList(sdkProvider))
    .subcommands(PartyDetails(sdkProvider))
    .subcommands(CurrentPairs(sdkProvider))
    .subcommands(Players(sdkProvider))
    .subcommands(
        Contribution()
            .subcommands(SaveContribution(clock = Clock.System, sdkProvider = sdkProvider))
            .subcommands(BatchContribution(sdkProvider = sdkProvider))
            .subcommands(ListContribution(sdkProvider = sdkProvider)),
    )
