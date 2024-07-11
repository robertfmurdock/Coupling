package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.cli.cliScope
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.tools.digger.json.ContributionParser
import com.zegreatrob.tools.digger.model.Contribution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

data class ContributionContext(val partyId: PartyId, val env: String)

class Contribution : CliktCommand() {
    private val env by option().default("production")
    private val partyId by option().required()
    override fun run() {
        currentContext.findOrSetObject { ContributionContext(PartyId(partyId), env) }
    }
}

class SaveContribution(
    private val scope: CoroutineScope = cliScope,
    private val cannon: ActionCannon<CouplingSdkDispatcher>? = null,
) : CliktCommand(name = "save"),
    ContributionCliCommand {
    private val inputJson by option().prompt()
    private val contributionId by option().default("")
    private val participantEmail by option().multiple()
    private val hash by option().default("")
    private val dateTime by option().default("")
    private val ease by option().default("")
    private val story by option().default("")
    override val link by option().default("")
    override val label by option().default("")
    override fun run() {
        val contributionContext = currentContext.findObject<ContributionContext>()
        val partyId = contributionContext!!.partyId
        val data = inputJson.trim()
        withSdk(
            scope = scope,
            env = contributionContext.env,
            echo = ::echo,
            cannon = cannon,
        ) { sdk ->
            if (data.isNotBlank()) {
                val contribution = ContributionParser.parseContribution(data)
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
                        dateTime = dateTime.ifBlank { null }?.let(Instant.Companion::parse),
                        ease = ease.ifBlank { null }?.toInt(),
                        story = story.ifBlank { null },
                        link = link.ifBlank { null },
                    ),
                )
            }
        }
    }
}

interface ContributionCliCommand {
    val label: String
    val link: String
}

class BatchContribution :
    CliktCommand(name = "batch"),
    ContributionCliCommand {
    private val inputJson by option().prompt()
    override val label by option().default("")
    override val link by option().default("")
    override fun run() {
        val contributions = ContributionParser.parseContributions(inputJson.trim())

        val contributionContext = currentContext.findObject<ContributionContext>()
        val partyId = contributionContext!!.partyId
        withSdk(cliScope, contributionContext.env, ::echo) { sdk ->
            coroutineScope {
                contributions.forEach { contribution ->
                    launch { sdk.fire(saveContributionCommand(partyId, contribution)) }
                }
            }
        }
    }
}

private fun ContributionCliCommand.saveContributionCommand(
    partyId: PartyId,
    contribution: Contribution,
) = SaveContributionCommand(
    partyId = partyId,
    contributionId = contribution.firstCommit,
    participantEmails = contribution.authors.toSet(),
    hash = contribution.lastCommit,
    dateTime = contribution.dateTime,
    ease = contribution.ease,
    story = contribution.storyId?.ifBlank { null },
    link = link.takeIf(String::isNotBlank),
    firstCommit = contribution.firstCommit,
    semver = contribution.semver,
    label = label.takeIf(String::isNotBlank) ?: contribution.label,
)
