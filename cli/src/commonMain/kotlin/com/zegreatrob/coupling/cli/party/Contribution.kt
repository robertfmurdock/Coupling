package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.model.party.PartyId

data class ContributionContext(val partyId: PartyId, val env: String)

class Contribution : SuspendingCliktCommand() {
    private val env by option().default("production")
    val partyId by requireObject<PartyId>("partyId")
    override suspend fun run() {
        currentContext.findOrSetObject { ContributionContext(partyId, env) }
    }
}

expect fun loadFile(path: String): String?

interface ContributionCliCommand {

    val label: String
    val link: String
}
