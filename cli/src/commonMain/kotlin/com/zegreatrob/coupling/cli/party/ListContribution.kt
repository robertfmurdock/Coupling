package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.SdkProvider
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.PartyContributionReportContributionsQuery
import com.zegreatrob.tools.digger.json.toJsonString
import com.zegreatrob.tools.digger.model.Contribution as DiggerContribution

class ListContribution(
    private val sdkProvider: SdkProvider,
) : SuspendingCliktCommand(name = "list") {
    private val contributionContext by requireObject<ContributionContext>()
    private val partyId: PartyId by requireObject<PartyId>("partyId")
    private val file by option().default("")
    private val json by option().flag()

    override suspend fun run() {
        if (!json) {
            throw CliktError("Only --json output is currently supported.", printError = true)
        }
        withSdk(env = contributionContext.env, echo = ::echo, sdkProvider = sdkProvider) { sdk ->
            val contributions = sdk.fire(GqlQuery(PartyContributionReportContributionsQuery(partyId)))
                ?.party
                ?.contributionReport
                ?.contributions
                ?.map { it.contributionDetails.toDomain() }
                ?: throw CliktError("Party not found.", printError = true)
            val output = contributions
                .map(Contribution::toDownloadContribution)
                .toJsonString()
            if (file.isBlank()) {
                echo(output)
            } else {
                writeFile(file, output)
            }
        }
    }
}

private fun Contribution.toDownloadContribution() = DiggerContribution(
    lastCommit = hash.orEmpty(),
    firstCommit = firstCommit.orEmpty(),
    authors = participantEmails.toList(),
    dateTime = dateTime,
    ease = ease,
    storyId = story,
    semver = semver,
    label = label,
    firstCommitDateTime = firstCommitDateTime,
    tagName = name,
    tagDateTime = integrationDateTime,
    commitCount = commitCount ?: 0,
)

expect fun writeFile(path: String, content: String)
