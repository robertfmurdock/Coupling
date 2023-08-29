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
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.ZonedDateTime

class Contribution : CliktCommand() {
    private val env by option().default("production")
    private val partyId by option().required()
    override fun run() {
        currentContext.findOrSetObject { PartyId(partyId) }
    }
}

class SaveContribution : CliktCommand(name = "save") {
    private val env by option().default("production")
    private val contributionId by option().required()
    private val participantEmail by option().multiple(required = true)
    private val hash by option().default("")
    private val dateTime by option().default("")
    private val ease by option().default("")
    private val story by option().default("")
    private val link by option().default("")
    override fun run() {
        val partyId = currentContext.findObject<PartyId>()!!
        withSdk(env, ::echo) { sdk ->
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
    private val env by option().default("production")
    private val inputJson by option().prompt()
    override fun run() {
        val partyId = currentContext.findObject<PartyId>()!!
        val jsonElement = Json.parseToJsonElement(inputJson.trim())

        val array = jsonElement.jsonArray

        withSdk(env, ::echo) { sdk ->
            coroutineScope {
                array.forEach { contribution ->
                    launch { saveContribution(sdk, partyId, contribution) }
                }
            }
        }
    }

    private suspend fun saveContribution(
        sdk: ActionCannon<CouplingSdkDispatcher>,
        partyId: PartyId,
        contribution: JsonElement,
    ) {
        val contributionId = contribution.jsonObject["lastCommit"]?.jsonPrimitive?.content
            ?: return echo("No last commit")
        val authors = contribution.jsonObject["authors"]?.jsonArray?.map { it.jsonPrimitive.content }
            ?: return echo("No authors")
        val dateTime = contribution.jsonObject["dateTime"]?.jsonPrimitive?.content
            ?: return echo("No dateTime")
        val ease = contribution.jsonObject["ease"]?.jsonPrimitive?.content
        val story = contribution.jsonObject["story"]?.jsonPrimitive?.content
        val link = contribution.jsonObject["link"]?.jsonPrimitive?.content

        sdk.fire(
            SaveContributionCommand(
                partyId = partyId,
                contributionId = contributionId,
                participantEmails = authors.toSet(),
                hash = contributionId,
                dateTime = dateTime.ifBlank { null }?.let { ZonedDateTime.parse(it) }?.toInstant()?.toKotlinInstant(),
                ease = ease?.ifBlank { null }?.toIntOrNull(),
                story = story?.ifBlank { null },
                link = link?.ifBlank { null },
            ),
        )
    }
}
