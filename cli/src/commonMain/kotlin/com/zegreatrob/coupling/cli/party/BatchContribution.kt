package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.cli.cliScope
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.tools.digger.json.ContributionParser
import kotlinx.datetime.Clock

class BatchContribution(
    private val clock: Clock,
) : CliktCommand(name = "batch"),
    ContributionCliCommand {
    private val file by option().default("")
    override val label by option().default("")
    override val link by option().default("")
    private val cycleTimeFromFirstCommit by option().flag()
    override fun run() {
        val inputJson = loadFile(file) ?: error("Could not load file")
        val contributions = ContributionParser.parseContributions(inputJson.trim())
        val contributionContext = currentContext.findObject<ContributionContext>()
        val partyId = contributionContext!!.partyId
        val inputs = contributions.map { contribution ->
            contribution.contributionInput(
                link = link.takeIf(String::isNotBlank),
                label = label.takeIf(String::isNotBlank),
                cycleTime = if (cycleTimeFromFirstCommit) {
                    cycleTimeFromFirstCommit(contribution, clock.now())
                } else {
                    null
                },
            )
        }
        val commands = inputs.chunked(100).map { SaveContributionCommand(partyId = partyId, contributionList = it) }
        withSdk(cliScope, contributionContext.env, ::echo) { sdk ->
            commands.forEach { command ->
                sdk.fire(command)
            }
        }
    }
}
