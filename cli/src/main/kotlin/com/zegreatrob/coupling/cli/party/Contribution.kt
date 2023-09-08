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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinInstant
import java.time.ZonedDateTime

data class ContributionContext(val partyId: PartyId, val env: String)

class Contribution : CliktCommand() {
    private val env by option().default("production")
    private val partyId by option().required()
    override fun run() {
        currentContext.findOrSetObject { ContributionContext(PartyId(partyId), env) }
    }
}

class SaveContribution : CliktCommand(name = "save") {
    private val contributionId by option().required()
    private val participantEmail by option().multiple(required = true)
    private val hash by option().default("")
    private val dateTime by option().default("")
    private val ease by option().default("")
    private val story by option().default("")
    private val link by option().default("")
    override fun run() {
        val contributionContext = currentContext.findObject<ContributionContext>()
        val partyId = contributionContext!!.partyId
        withSdk(contributionContext.env, ::echo) { sdk ->
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

class BatchContribution : CliktCommand(name = "batch") {
    private val inputJson by option().prompt()
    override fun run() {
        val contributions = ContributionParser.parseContributions(inputJson.trim())

        val contributionContext = currentContext.findObject<ContributionContext>()
        val partyId = contributionContext!!.partyId
        withSdk(contributionContext.env, ::echo) { sdk ->
            coroutineScope {
                contributions.forEach { contribution ->
                    launch {
                        sdk.fire(
                            SaveContributionCommand(
                                partyId = partyId,
                                contributionId = contribution.lastCommit,
                                participantEmails = contribution.authors.toSet(),
                                hash = contribution.lastCommit,
                                dateTime = contribution.dateTime?.ifBlank { null }?.let { ZonedDateTime.parse(it) }
                                    ?.toInstant()
                                    ?.toKotlinInstant(),
                                ease = contribution.ease,
                                story = contribution.storyId?.ifBlank { null },
                                link = null,
                            ),
                        )
                    }
                }
            }
        }
    }
}
