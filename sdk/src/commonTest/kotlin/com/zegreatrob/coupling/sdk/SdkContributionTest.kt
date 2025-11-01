package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Optional
import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.stats.halfwayValue
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.repository.validation.assertIsCloseToNow
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.schema.PartyContributionReportContributionsQuery
import com.zegreatrob.coupling.sdk.schema.PartyContributionReportContributorsQuery
import com.zegreatrob.coupling.sdk.schema.PartyContributionReportNumbersQuery
import com.zegreatrob.coupling.sdk.schema.PartyPairsContributionReportContributionsQuery
import com.zegreatrob.coupling.sdk.schema.PartyPairsContributionReportNumbersQuery
import com.zegreatrob.coupling.sdk.schema.type.ContributionWindow
import com.zegreatrob.coupling.sdk.schema.type.ContributionsInput
import com.zegreatrob.coupling.sdk.schema.type.PairsInput
import com.zegreatrob.coupling.stubmodel.roundToMillis
import com.zegreatrob.coupling.stubmodel.stubContributionInput
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SdkContributionTest {

    @Test
    fun canSaveAndQueryContributions() = asyncSetup(object {
        val party = stubPartyDetails()
        val contributionInputs = generateSequence {
            ContributionInput(
                contributionId = ContributionId.new(),
                participantEmails = setOf(uuidString(), uuidString(), uuidString()),
                hash = uuidString(),
                dateTime = Clock.System.now().minus(Random.nextInt(60 * 60).seconds).roundToMillis(),
                ease = Random.nextInt(),
                story = uuidString(),
                link = uuidString(),
                commitCount = null,
                name = null,
            )
        }.take(3).toList()
    }) {
        savePartyState(party, emptyList(), emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyContributionReportContributionsQuery(party.id)))
    } verify { result ->
        val contributions = result?.party?.contributionReport?.contributions?.map { it.contributionFragment.toModel() }
        contributions
            ?.withoutCreatedAt()
            .assertIsEqualTo(
                contributionInputs.toExpectedContributions(),
            )
        contributions?.map { it.createdAt }
            ?.forEach { createdAt ->
                createdAt.assertIsCloseToNow()
            }
    }

    @Test
    fun queryBasicContributionStatistics() = asyncSetup(object {
        val party = stubPartyDetails()
        val cycleTimeContributionsCount = 25
        val contributionInputs = generateSequence { stubContributionInput() }.take(cycleTimeContributionsCount)
            .toList() + stubContributionInput().copy(cycleTime = null)
    }) {
        savePartyState(party, emptyList(), emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyContributionReportNumbersQuery(party.id)))
            ?.party
            ?.contributionReport
    } verify { result ->
        result?.count.assertIsEqualTo(contributionInputs.size)
        result?.medianCycleTime.assertIsEqualTo(contributionInputs.mapNotNull { it.cycleTime }.sorted().halfwayValue())
        result?.withCycleTimeCount.assertIsEqualTo(cycleTimeContributionsCount)
    }

    @Test
    fun clearingContributionsWillRemoveThemFromParty() = asyncSetup(object {
        val party = stubPartyDetails()
        val contributionInputs = generateSequence {
            ContributionInput(
                contributionId = ContributionId.new(),
                participantEmails = setOf(uuidString(), uuidString(), uuidString()),
                hash = uuidString(),
                dateTime = Clock.System.now().minus(Random.nextInt(60 * 60).seconds).roundToMillis(),
                ease = Random.nextInt(),
                story = uuidString(),
                link = uuidString(),
                commitCount = null,
                name = null,
            )
        }.take(4).toList()
    }) {
        savePartyState(party, emptyList(), emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
        sdk().fire(ClearContributionsCommand(partyId = party.id))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyContributionReportContributionsQuery(party.id)))
    } verify { result ->
        result?.party?.contributionReport?.contributions?.size
            .assertIsEqualTo(0)
    }

    @Test
    fun canQueryContributionsByPair() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val expectedPlayer = players[1]
        val contributionInput = stubContributionInput()
            .copy(participantEmails = setOf(expectedPlayer.email))
        val contributionInputs = listOf(
            stubContributionInput().copy(participantEmails = setOf()),
            contributionInput,
            stubContributionInput().copy(participantEmails = setOf()),
        )
    }) {
        savePartyState(party, players, emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(
            ApolloGraphQuery(
                PartyPairsContributionReportContributionsQuery(
                    partyId = party.id,
                    reportInput = Optional.present(
                        ContributionsInput(window = Optional.present(ContributionWindow.Week)),
                    ),
                ),
            ),
        )
    } verify { result ->
        result?.party?.pairs
            ?.filter { it.players?.map { player -> player.id } != listOf(expectedPlayer.id) }
            ?.forEach {
                val contributions = it.contributionReport?.contributions
                    ?.map { contribution -> contribution.contributionFragment.toModel() }
                contributions?.withoutCreatedAt()
                    .assertIsEqualTo(
                        emptyList(),
                        "Pairs should only contain contributions with exact matches, but ${
                            it.players?.map { player -> player.id }
                        } had an inappropriate match",
                    )
            }
        result?.party?.pairs
            ?.find { it.players?.map { player -> player.id } == listOf(expectedPlayer.id) }
            ?.contributionReport?.contributions?.map { it.contributionFragment.toModel() }
            ?.withoutCreatedAt()
            .assertIsEqualTo(
                listOf(
                    contributionInput.toExpectedContribution(),
                ),
                "Contribution should be included in solo correctly",
            )
    }

    @Test
    fun queryPairContributionStatistics() = asyncSetup(object {
        val party = stubPartyDetails()
        val cycleTimeContributionsCount = 25
        val email1 = uuidString()
        val email2 = uuidString()
        val pairEmails = setOf(email1, email2)
        val contributionInputs = generateSequence {
            stubContributionInput()
                .copy(participantEmails = pairEmails)
        }.take(cycleTimeContributionsCount)
            .toList() + stubContributionInput().copy(
            cycleTime = null,
            participantEmails = pairEmails,
        )
    }) {
        savePartyState(party, emptyList(), emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs + stubContributionInput()))
    } exercise {
        sdk().fire(
            ApolloGraphQuery(
                PartyPairsContributionReportNumbersQuery(
                    partyId = party.id,
                    pairsInput = Optional.present(PairsInput(includeRetired = Optional.present(true))),
                ),
            ),
        )
            ?.party?.pairs?.first {
                it.players?.map { player -> player.email }?.toSet() == pairEmails
            }?.contributionReport
    } verify { result ->
        result?.medianCycleTime.assertIsEqualTo(contributionInputs.mapNotNull { it.cycleTime }.sorted().halfwayValue())
        result?.withCycleTimeCount.assertIsEqualTo(cycleTimeContributionsCount)
        result?.count.assertIsEqualTo(contributionInputs.size)
    }

    @Test
    fun canQueryContributionsInWindow() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val expectedPlayer = players[1]
        val excludedContributionInput = stubContributionInput()
            .copy(
                dateTime = Clock.System.now().minus(8.days),
                integrationDateTime = null,
                participantEmails = setOf(expectedPlayer.email),
            )
        val contributionInputs = listOf(
            stubContributionInput(),
            excludedContributionInput,
            stubContributionInput(),
        )
    }) {
        savePartyState(party, players, emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(
            ApolloGraphQuery(
                PartyContributionReportContributionsQuery(
                    partyId = party.id,
                    reportInput = Optional.present(ContributionsInput(window = Optional.present(ContributionWindow.Week))),
                ),
            ),
        )
    } verify { result ->
        result?.party
            ?.contributionReport?.contributions?.map { it.contributionFragment.toModel() }
            ?.withoutCreatedAt()
            ?.toSet()
            .assertIsEqualTo(
                (contributionInputs - excludedContributionInput)
                    .map(ContributionInput::toExpectedContribution)
                    .toSet(),
                "Old contributions should be excluded",
            )
    }

    @Test
    fun canQueryContributionsWithLimit() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val now = Clock.System.now().roundToMillis()
        val contributionInputs = (0..12).map { number ->
            now.minus(number.days)
        }.map { dateTime ->
            stubContributionInput().copy(dateTime = dateTime, integrationDateTime = dateTime)
        }
        val expectedLimit = 6
    }) {
        savePartyState(party, players, emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(
            ApolloGraphQuery(
                PartyContributionReportContributionsQuery(
                    partyId = party.id,
                    reportInput = Optional.present(ContributionsInput(limit = Optional.present(expectedLimit))),
                ),
            ),
        )
    } verify { result ->
        result?.party
            ?.contributionReport
            ?.contributions
            ?.map { it.contributionFragment.toModel() }
            ?.withoutCreatedAt()
            ?.toSet()
            .assertIsEqualTo(
                contributionInputs.take(expectedLimit)
                    .map(ContributionInput::toExpectedContribution)
                    .toSet(),
                "Older contributions should be excluded",
            )
    }

    @Test
    fun canQueryContributionsByPairInWindow() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val expectedPlayer = players[1]
        val excludedContributionCommand = stubContributionInput()
            .copy(
                dateTime = Clock.System.now().minus(8.days),
                participantEmails = setOf(expectedPlayer.email),
            )
        val contributionInputs = listOf(
            stubContributionInput().copy(participantEmails = setOf(expectedPlayer.email)),
            excludedContributionCommand,
            stubContributionInput().copy(participantEmails = setOf(expectedPlayer.email)),
        )
    }) {
        savePartyState(party, players, emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(
            ApolloGraphQuery(
                PartyPairsContributionReportContributionsQuery(
                    partyId = party.id,
                    reportInput = Optional.present(
                        ContributionsInput(window = Optional.present(ContributionWindow.Week)),
                    ),
                ),
            ),
        )
    } verify { result ->
        result?.party?.pairs
            ?.find { it.players?.map { player -> player.id } == listOf(expectedPlayer.id) }
            ?.contributionReport?.contributions?.map { it.contributionFragment.toModel() }
            ?.withoutCreatedAt()
            ?.toSet()
            .assertIsEqualTo(
                (contributionInputs - excludedContributionCommand)
                    .map(ContributionInput::toExpectedContribution)
                    .toSet(),
                "Old contributions should be excluded",
            )
    }

    @Test
    fun canQueryContributorsThatAreNotPlayers() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val contributionInputs = listOf(
            stubContributionInput(),
            stubContributionInput(),
            stubContributionInput(),
        )
    }) {
        savePartyState(party, players, emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyContributionReportContributorsQuery(party.id)))
    } verify { result ->
        val contributors = result?.party?.contributionReport?.contributors
        contributors?.map { it.email }
            .assertIsEqualTo(
                contributionInputs.asSequence().flatMap { it.participantEmails }
                    .toSet()
                    .sorted(),
            )
        contributors?.mapNotNull { it.playerId }.assertIsEqualTo(emptyList())
    }

    @Test
    fun canQueryContributorsThatArePlayers() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val contributionInputs = listOf(
            stubContributionInput().copy(participantEmails = players.map { it.email }.toSet()),
            stubContributionInput().copy(participantEmails = setOf(players.random().email)),
            stubContributionInput().copy(participantEmails = setOf(players.random().email)),
        )
    }) {
        savePartyState(party, players, emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyContributionReportContributorsQuery(party.id)))
    } verify { result ->
        result?.party?.contributionReport?.contributors?.map { it.playerId }
            .assertIsEqualTo(
                players.sortedBy { it.email }.map { it.id },
            )
    }

    @Test
    fun canQueryContributorsWithMoreThanOneEmailCollapseToOnePlayer() = asyncSetup(object {
        val party = stubPartyDetails()
        val additionalEmail1 = uuidString()
        val additionalEmail2 = uuidString()
        val player = stubPlayer().copy(additionalEmails = setOf(additionalEmail1, additionalEmail2))
        val contributionInputs = listOf(
            stubContributionInput().copy(participantEmails = setOf(player.email)),
            stubContributionInput().copy(participantEmails = setOf(additionalEmail1)),
            stubContributionInput().copy(participantEmails = setOf(additionalEmail2)),
        )
    }) {
        savePartyState(party, listOf(player), emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyContributionReportContributorsQuery(party.id)))
    } verify { result ->
        result?.party?.contributionReport?.contributors?.map { it.playerId }
            .assertIsEqualTo(
                listOf(player.id),
            )
    }

    @Test
    fun canQueryContributorsThatArePlayersViaUnverifiedEmail() = asyncSetup(object {
        val party = stubPartyDetails()
        val unvalidatedEmail = uuidString()
        val player = stubPlayer().copy(additionalEmails = setOf(unvalidatedEmail))
        val contributionInputs = listOf(
            stubContributionInput().copy(participantEmails = setOf(unvalidatedEmail)),
        )
    }) {
        savePartyState(party, listOf(player), emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyContributionReportContributorsQuery(party.id)))
    } verify { result ->
        result?.party?.contributionReport?.contributors?.map { it.playerId }
            .assertIsEqualTo(listOf(player.id))
    }

    @Test
    fun canQueryContributorsThatArePlayersViaUnverifiedEmailWithDifferentCasing() = asyncSetup(object {
        val party = stubPartyDetails()
        val unvalidatedEmail = uuidString()
        val player = stubPlayer().copy(additionalEmails = setOf(unvalidatedEmail))
        val contributionInputs = listOf(
            stubContributionInput().copy(participantEmails = setOf(unvalidatedEmail.uppercase())),
        )
    }) {
        savePartyState(party, listOf(player), emptyList())
        sdk().fire(SaveContributionCommand(party.id, contributionInputs))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyContributionReportContributorsQuery(party.id)))
    } verify { result ->
        result?.party?.contributionReport?.contributors?.map { it.playerId }
            .assertIsEqualTo(listOf(player.id))
    }
}

private fun List<Contribution>.withoutCreatedAt(): List<Contribution> = map {
    it.copy(createdAt = Instant.DISTANT_PAST)
}

private fun List<ContributionInput>.toExpectedContributions() = map { it.toExpectedContribution() }
    .sortedByDescending { it.dateTime }

private fun ContributionInput.toExpectedContribution() = Contribution(
    id = contributionId,
    createdAt = Instant.DISTANT_PAST,
    dateTime = dateTime,
    hash = hash,
    ease = ease,
    story = story,
    link = link,
    participantEmails = participantEmails,
    label = label,
    firstCommit = firstCommit,
    semver = semver,
    firstCommitDateTime = firstCommitDateTime,
    integrationDateTime = integrationDateTime,
    cycleTime = cycleTime,
    name = name,
    commitCount = commitCount,
)
