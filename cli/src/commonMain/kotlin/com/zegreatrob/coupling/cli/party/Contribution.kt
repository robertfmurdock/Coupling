package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.tools.digger.json.ContributionParser
import com.zegreatrob.tools.digger.model.Contribution
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

data class ContributionContext(val partyId: PartyId, val env: String)

class Contribution : CliktCommand() {
    private val env by option().default("production")
    private val partyId by option().required()
    override fun run() {
        currentContext.findOrSetObject { ContributionContext(PartyId(partyId), env) }
    }
}

class SaveContribution : CliktCommand(name = "save") {
    private val input by option().prompt()
    private val contributionId by option().default("")
    private val participantEmail by option().multiple()
    private val hash by option().default("")
    private val dateTime by option().default("")
    private val ease by option().default("")
    private val story by option().default("")
    private val link by option().default("")
    override fun run() {
        val contributionContext = currentContext.findObject<ContributionContext>()
        val partyId = contributionContext!!.partyId

        withSdk(contributionContext.env, ::echo) { sdk ->
            if (input.isNotBlank()) {
                val contribution = ContributionParser.parseContribution(input.trim())
                if (contribution != null) {
                    sdk.fire(saveContributionCommand(partyId, contribution))
                } else {
                    echo("Could not parse contribution", err = true)
                }
            } else {
                sdk.fire(
                    SaveContributionCommand(
                        partyId = partyId,
                        contributionId = contributionId,
                        participantEmails = participantEmail.toSet(),
                        hash = hash,
                        dateTime = dateTime.ifBlank { null }?.toInstant(),
                        ease = ease.ifBlank { null }?.toInt(),
                        story = story.ifBlank { null },
                        link = link.ifBlank { null },
                    ),
                )
            }
        }
    }
}

class BatchContribution : CliktCommand(name = "batch") {
    private val inputJson by option().prompt()
    override fun run() {
        val contributions = ContributionParser.parseContributions(inputJson.trim())

        val contributionContext = currentContext.findObject<ContributionContext>()
        val partyId = contributionContext!!.partyId
        withSdk(contributionContext.env, ::echo) { sdk ->
            coroutineScope {
                contributions.forEach { contribution ->
                    launch { sdk.fire(saveContributionCommand(partyId, contribution)) }
                }
            }
        }
    }
}

private fun saveContributionCommand(
    partyId: PartyId,
    contribution: Contribution,
) = SaveContributionCommand(
    partyId = partyId,
    contributionId = contribution.firstCommit,
    participantEmails = contribution.authors.toSet(),
    hash = contribution.firstCommit,
    dateTime = contribution.dateTime?.ifBlank { null }?.let { Instant.parse(it) },
    ease = contribution.ease,
    story = contribution.storyId?.ifBlank { null },
    link = null,
)
