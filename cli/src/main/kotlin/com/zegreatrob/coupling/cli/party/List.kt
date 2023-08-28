package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.graphQuery
import kotlinx.datetime.toInstant

class List : CliktCommand() {
    private val env by option().default("production")
    override fun run() {
        withSdk(env, ::echo) { sdk ->
            sdk.fire(graphQuery { partyList { details() } })
                ?.partyList
                ?.mapNotNull { it.details?.data }
                ?.joinToString("\n") { "Party: id = ${it.id.value}, name = ${it.name}" }
                .let { it ?: "" }
                .let { echo(it) }
        }
    }
}

class Contribution : CliktCommand() {
    private val env by option().default("production")
    private val partyId by option().required()
    private val contributionId by option().required()
    private val participantEmail by option().multiple(required = true)
    private val hash by option().default("")
    private val dateTime by option().default("")
    private val ease by option().default("")
    private val story by option().default("")
    private val link by option().default("")
    override fun run() {
        withSdk(env, ::echo) { sdk ->
            val partyId = partyId.let(::PartyId)
            sdk.fire(
                SaveContributionCommand(
                    partyId = partyId,
                    contributionId = contributionId,
                    participantEmails = participantEmail,
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
