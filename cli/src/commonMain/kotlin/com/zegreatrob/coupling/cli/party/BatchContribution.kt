package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.tools.digger.json.ContributionParser

class BatchContribution(
    private val cannon: ActionCannon<CouplingSdkDispatcher>? = null,
) : SuspendingCliktCommand(name = "batch"),
    ContributionCliCommand {
    private val file by option().default("")
    private val inputJson by option()
    override val label by option().default("")
    override val link by option().default("")
    private val cycleTimeFromFirstCommit by option().flag()
    override suspend fun run() {
        val inputJson = inputJson ?: loadFile(file) ?: error("Could not load file")
        val contributions = ContributionParser.parseContributions(inputJson.trim())
        val contributionContext = currentContext.findObject<ContributionContext>()
        val partyId = contributionContext!!.partyId
        val inputs = contributions.map { contribution ->
            contribution.contributionInput(
                link = link.takeIf(String::isNotBlank),
                label = label.takeIf(String::isNotBlank),
                cycleTime = if (cycleTimeFromFirstCommit) {
                    cycleTimeFromFirstCommit(contribution, null)
                } else {
                    null
                },
            )
        }
        val commands = inputs.chunked(100).map { SaveContributionCommand(partyId = partyId, contributionList = it) }
        withSdk(contributionContext.env, ::echo, cannon = cannon) { sdk ->
            commands.forEach { command ->
                sdk.fire(command)
            }
        }
    }
}
