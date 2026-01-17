package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.cli.cliScope
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.tools.digger.json.ContributionParser
import kotlinx.coroutines.CoroutineScope
import kotools.types.text.toNotBlankString
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class SaveContribution(
    private val scope: CoroutineScope = cliScope,
    private val cannon: ActionCannon<CouplingSdkDispatcher>? = null,
    private val clock: Clock,
) : SuspendingCliktCommand(name = "save"),
    ContributionCliCommand {
    private val inputJson by option().prompt()
    private val contributionId by option().default("")
    private val participantEmail by option().multiple()
    private val hash by option().default("")
    private val dateTime by option().default("")
    private val ease by option().default("")
    private val story by option().default("")
    private val cycleTimeFromFirstCommit by option().flag()
    private val cycleTime by option().default("")
    override val link by option().default("")
    override val label by option().default("")
    override suspend fun run() {
        val contributionContext = currentContext.findObject<ContributionContext>()
        val partyId = contributionContext!!.partyId
        val data = inputJson.trim()
        val action = SaveContributionCommand(
            partyId = partyId,
            contributionList = listOfNotNull(
                if (data.isNotBlank()) {
                    val contribution = ContributionParser.parseContribution(data)
                    contribution?.contributionInput(
                        link = link.takeIf(String::isNotBlank),
                        label = label.takeIf(String::isNotBlank),
                        cycleTime = if (cycleTimeFromFirstCommit) {
                            cycleTimeFromFirstCommit(contribution, clock.now())
                        } else {
                            cycleTime.ifBlank { null }
                                ?.let(Duration.Companion::parse)
                        },
                    )
                } else {
                    ContributionInput(
                        contributionId = ContributionId(contributionId.toNotBlankString().getOrThrow()),
                        participantEmails = participantEmail.toSet(),
                        hash = hash,
                        dateTime = dateTime.ifBlank { null }?.let(Instant.Companion::parse),
                        ease = ease.ifBlank { null }?.toInt(),
                        story = story.ifBlank { null },
                        link = link.ifBlank { null },
                        commitCount = null,
                        name = null,
                    )
                },
            ),
        )
        if (action.contributionList.isEmpty()) {
            echo("Could not parse contribution", err = true)
        } else {
            withSdk(
                scope = scope,
                env = contributionContext.env,
                echo = ::echo,
                cannon = cannon,
            ) { sdk -> sdk.fire(action) }
        }
    }
}
