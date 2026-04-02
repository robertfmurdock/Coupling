package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.test
import com.zegreatrob.coupling.cli.CouplingCliConfig
import com.zegreatrob.coupling.cli.cli
import com.zegreatrob.coupling.cli.createTempDirectory
import com.zegreatrob.coupling.cli.writeToFile
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.PartyContributionReportContributionsQuery
import com.zegreatrob.coupling.sdk.schema.type.buildContribution
import com.zegreatrob.coupling.sdk.schema.type.buildContributionReport
import com.zegreatrob.coupling.sdk.schema.type.buildParty
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.tools.digger.json.toJsonString
import kotlinx.serialization.json.Json
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import com.zegreatrob.tools.digger.model.Contribution as DiggerContribution

class ListContributionCLITest {

    @Test
    fun willReturnPartyContributionBatchAsJsonWhenJsonIsRequested() = asyncSetup(object : ScopeMint() {
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val expectedQuery = GqlQuery(PartyContributionReportContributionsQuery(partyId))
        val contributions = listOf(
            stubStoredContribution(
                idSeed = "one",
                participantEmails = setOf("alice@example.com", "bob@example.com"),
                hash = "last-commit-one",
                firstCommit = "first-commit-one",
                label = "Release 1",
                story = "STORY-1",
                semver = "1.0.0",
                name = "release-1.0.0",
            ),
            stubStoredContribution(
                idSeed = "two",
                participantEmails = setOf("carol@example.com"),
                hash = "last-commit-two",
                firstCommit = "first-commit-two",
                label = "Release 2",
                story = "STORY-2",
                semver = "2.0.0",
                name = "release-2.0.0",
            ),
        )
        val expectedOutput = contributions.map(Contribution::toDownloadContribution).toJsonString()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also {
                it.given(
                    expectedQuery,
                    contributionQueryResult(
                        partyId = partyId,
                        contributions = contributions,
                    ),
                )
            }
    }) exercise {
        cli(cannon).test("party --party-id=${partyId.value} contribution list --json")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        receivedActions.firstOrNull()
            .assertIsEqualTo(expectedQuery)
        result.output.trim()
            .assertIsEqualTo(expectedOutput)
    }

    @Test
    fun givenPartyIsConfiguredWillWritePartyContributionBatchToFileWhenJsonIsRequested() = asyncSetup(object : ScopeMint() {
        val workingDir = createTempDirectory()
        val outputFile = "$workingDir/contributions.json"
        val configFile = "$workingDir/.coupling"
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val expectedQuery = GqlQuery(PartyContributionReportContributionsQuery(partyId))
        val contributions = listOf(
            stubStoredContribution(
                idSeed = "configured",
                participantEmails = setOf("configured@example.com"),
                hash = "last-commit-configured",
                firstCommit = "first-commit-configured",
                label = "Configured Release",
                story = "STORY-CONFIGURED",
                semver = "3.0.0",
                name = "release-3.0.0",
            ),
        )
        val expectedOutput = contributions.map(Contribution::toDownloadContribution).toJsonString()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also {
                it.given(
                    expectedQuery,
                    contributionQueryResult(
                        partyId = partyId,
                        contributions = contributions,
                    ),
                )
            }
    }) {
        Json.encodeToString(CouplingCliConfig(partyId = partyId))
            .writeToFile(configFile)
    } exercise {
        cli(cannon)
            .test(
                "party contribution list --json --file=$outputFile",
                envvars = mapOf("PWD" to workingDir),
            )
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        receivedActions.firstOrNull()
            .assertIsEqualTo(expectedQuery)
        loadFile(outputFile)
            .assertIsEqualTo(expectedOutput)
    }

    @Test
    fun whenJsonIsNotRequestedReturnFailure() = asyncSetup(object : ScopeMint() {
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
    }) exercise {
        cli(cannon).test("party --party-id=${partyId.value} contribution list")
    } verify { result ->
        result.statusCode.assertIsEqualTo(1, result.output)
        result.output.trim()
            .assertIsEqualTo("Only --json output is currently supported.")
        receivedActions.isEmpty()
            .assertIsEqualTo(true)
    }

    @Test
    fun whenRequestedPartyDoesNotExistReturnFailure() = asyncSetup(object : ScopeMint() {
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val expectedQuery = GqlQuery(PartyContributionReportContributionsQuery(partyId))
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also {
                it.given(
                    expectedQuery,
                    PartyContributionReportContributionsQuery.Data { party = null },
                )
            }
    }) exercise {
        cli(cannon).test("party --party-id=${partyId.value} contribution list --json")
    } verify { result ->
        result.statusCode.assertIsEqualTo(1, result.output)
        result.output.trim()
            .assertIsEqualTo("Party not found.")
    }
}

private fun contributionQueryResult(
    partyId: com.zegreatrob.coupling.model.party.PartyId,
    contributions: List<Contribution>,
) = PartyContributionReportContributionsQuery.Data {
    party = buildParty {
        id = partyId
        contributionReport = buildContributionReport {
            this.partyId = partyId
            this.count = contributions.size
            this.contributors = emptyList()
            this.contributions = contributions.map { contribution ->
                buildContribution {
                    id = contribution.id
                    this.partyId = partyId
                    createdAt = contribution.createdAt
                    cycleTime = contribution.cycleTime
                    dateTime = contribution.dateTime
                    ease = contribution.ease
                    firstCommit = contribution.firstCommit
                    firstCommitDateTime = contribution.firstCommitDateTime
                    hash = contribution.hash
                    integrationDateTime = contribution.integrationDateTime
                    label = contribution.label
                    link = contribution.link
                    name = contribution.name
                    participantEmails = contribution.participantEmails.toList()
                    semver = contribution.semver
                    story = contribution.story
                    commitCount = contribution.commitCount
                    timestamp = contribution.createdAt
                    isDeleted = false
                }
            }
        }
    }
}

private fun stubStoredContribution(
    idSeed: String,
    participantEmails: Set<String>,
    hash: String,
    firstCommit: String,
    label: String,
    story: String,
    semver: String,
    name: String,
) = Contribution(
    id = ContributionId("$idSeed-id".toNotBlankString().getOrThrow()),
    createdAt = Instant.parse("2024-01-15T10:15:30Z"),
    dateTime = Instant.parse("2024-01-15T09:30:00Z"),
    hash = hash,
    firstCommit = firstCommit,
    firstCommitDateTime = Instant.parse("2024-01-15T08:45:00Z"),
    ease = 7,
    story = story,
    link = "https://example.com/$idSeed",
    participantEmails = participantEmails,
    label = label,
    semver = semver,
    integrationDateTime = Instant.parse("2024-01-15T10:00:00Z"),
    cycleTime = 75.minutes,
    commitCount = 5,
    name = name,
)

private fun Contribution.toDownloadContribution() = DiggerContribution(
    lastCommit = hash!!,
    firstCommit = firstCommit!!,
    authors = participantEmails.toList(),
    dateTime = dateTime!!,
    ease = ease!!,
    storyId = story!!,
    semver = semver!!,
    label = label!!,
    firstCommitDateTime = firstCommitDateTime,
    tagName = name!!,
    tagDateTime = integrationDateTime,
    commitCount = commitCount!!,
)
