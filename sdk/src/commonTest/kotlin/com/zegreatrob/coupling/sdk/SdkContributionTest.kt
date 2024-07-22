package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.validation.assertIsCloseToNow
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.roundToMillis
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SdkContributionTest {

    @Test
    fun canSaveAndQueryContributions() = asyncSetup(object {
        val party = stubPartyDetails()
        val saveContributionCommands = generateSequence {
            SaveContributionCommand(
                partyId = party.id,
                contributionList = listOf(
                    ContributionInput(
                        contributionId = uuidString(),
                        participantEmails = setOf(uuidString(), uuidString(), uuidString()),
                        hash = uuidString(),
                        dateTime = Clock.System.now().minus(Random.nextInt(60 * 60).seconds).roundToMillis(),
                        ease = Random.nextInt(),
                        story = uuidString(),
                        link = uuidString(),
                    ),
                ),
            )
        }.take(3).toList()
    }) {
        savePartyState(party, emptyList(), emptyList())
        saveContributionCommands.forEach {
            sdk().fire(it)
        }
    } exercise {
        sdk().fire(graphQuery { party(party.id) { contributions() } })
    } verify { result ->
        result?.party?.contributions?.elements?.withoutCreatedAt()
            .assertIsEqualTo(
                saveContributionCommands.flatMap { it.contributionList }.toExpectedContributions(),
            )
        result?.party?.contributions?.elements?.map { it.createdAt }?.forEach { createdAt ->
            createdAt.assertIsCloseToNow()
        }
    }

    @Test
    fun clearingContributionsWillRemoveThemFromParty() = asyncSetup(object {
        val party = stubPartyDetails()
        val saveContributionCommands = generateSequence {
            SaveContributionCommand(
                partyId = party.id,
                contributionList = listOf(
                    ContributionInput(
                        contributionId = uuidString(),
                        participantEmails = setOf(uuidString(), uuidString(), uuidString()),
                        hash = uuidString(),
                        dateTime = Clock.System.now().minus(Random.nextInt(60 * 60).seconds).roundToMillis(),
                        ease = Random.nextInt(),
                        story = uuidString(),
                        link = uuidString(),
                    ),
                ),
            )
        }.take(4).toList()
    }) {
        savePartyState(party, emptyList(), emptyList())
        saveContributionCommands.forEach { sdk().fire(it) }
        sdk().fire(ClearContributionsCommand(partyId = party.id))
    } exercise {
        sdk().fire(graphQuery { party(party.id) { contributions() } })
    } verify { result ->
        result?.party?.contributions?.size
            .assertIsEqualTo(0)
    }

    @Test
    fun canQueryContributionsByPair() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val expectedPlayer = players[1]
        val contributionInput = stubContributionInput()
            .copy(participantEmails = setOf(expectedPlayer.email))
        val saveContributionCommands = listOf(
            stubContributionInput().copy(participantEmails = setOf()),
            contributionInput,
            stubContributionInput().copy(participantEmails = setOf()),
        ).map { SaveContributionCommand(partyId = party.id, listOf(it)) }
    }) {
        savePartyState(party, players, emptyList())
        saveContributionCommands.forEach {
            sdk().fire(it)
        }
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    pairs {
                        players()
                        contributions()
                    }
                }
            },
        )
    } verify { result ->
        result?.party?.pairs
            ?.filter { it.players?.elements?.map(Player::id) != listOf(expectedPlayer.id) }
            ?.forEach {
                it.contributions?.elements?.withoutCreatedAt()
                    .assertIsEqualTo(
                        emptyList(),
                        "Pairs should only contain contributions with exact matches, but ${
                            it.players?.elements?.map(Player::id)
                        } had an inappropriate match",
                    )
            }
        result?.party?.pairs
            ?.find { it.players?.elements?.map(Player::id) == listOf(expectedPlayer.id) }
            ?.contributions?.elements?.withoutCreatedAt()
            .assertIsEqualTo(
                listOf(
                    contributionInput.toExpectedContribution(),
                ),
                "Contribution should be included in solo correctly",
            )
    }

    @Test
    fun canQueryContributionsInWindow() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val expectedPlayer = players[1]
        val excludedContributionInput = stubContributionInput()
            .copy(
                dateTime = Clock.System.now().minus(8.days),
                participantEmails = setOf(expectedPlayer.email),
            )
        val saveContributionCommands = listOf(
            stubContributionInput(),
            excludedContributionInput,
            stubContributionInput(),
        ).map { SaveContributionCommand(party.id, listOf(it)) }
    }) {
        savePartyState(party, players, emptyList())
        saveContributionCommands.forEach {
            sdk().fire(it)
        }
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    contributions(JsonContributionWindow.Week)
                }
            },
        )
    } verify { result ->
        result?.party
            ?.contributions?.elements?.withoutCreatedAt()
            ?.toSet()
            .assertIsEqualTo(
                (saveContributionCommands.flatMap { it.contributionList } - excludedContributionInput)
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
        val saveContributionCommands = (0..12).map { number ->
            now.minus(number.days)
        }.map { dateTime ->
            stubContributionInput().copy(dateTime = dateTime)
        }.map { SaveContributionCommand(party.id, listOf(it)) }
        val expectedLimit = 6
    }) {
        savePartyState(party, players, emptyList())
        saveContributionCommands.forEach {
            setupScope.launch { sdk().fire(it) }
        }
    } exercise {
        sdk().fire(graphQuery { party(party.id) { contributions(limit = expectedLimit) } })
    } verify { result ->
        result?.party
            ?.contributions
            ?.elements
            ?.withoutCreatedAt()
            ?.toSet()
            .assertIsEqualTo(
                (saveContributionCommands.take(expectedLimit))
                    .flatMap { it.contributionList }
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
        val saveContributionCommands = listOf(
            stubContributionInput().copy(participantEmails = setOf(expectedPlayer.email)),
            excludedContributionCommand,
            stubContributionInput().copy(participantEmails = setOf(expectedPlayer.email)),
        ).map { SaveContributionCommand(party.id, listOf(it)) }
    }) {
        savePartyState(party, players, emptyList())
        saveContributionCommands.forEach {
            sdk().fire(it)
        }
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    pairs {
                        players()
                        contributions(JsonContributionWindow.Week)
                    }
                }
            },
        )
    } verify { result ->
        result?.party?.pairs
            ?.find { it.players?.elements?.map(Player::id) == listOf(expectedPlayer.id) }
            ?.contributions?.elements?.withoutCreatedAt()
            ?.toSet()
            .assertIsEqualTo(
                (saveContributionCommands.flatMap { it.contributionList } - excludedContributionCommand)
                    .map(ContributionInput::toExpectedContribution)
                    .toSet(),
                "Old contributions should be excluded",
            )
    }

    @Test
    fun canQueryContributorsThatAreNotPlayers() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val saveContributionCommands = listOf(
            stubSaveContributionCommand(party.id),
            stubSaveContributionCommand(party.id),
            stubSaveContributionCommand(party.id),
        )
    }) {
        savePartyState(party, players, emptyList())
        saveContributionCommands.forEach { sdk().fire(it) }
    } exercise {
        sdk().fire(
            graphQuery { party(party.id) { contributors { email() } } },
        )
    } verify { result ->
        result?.party?.contributors
            .assertIsEqualTo(
                saveContributionCommands.asSequence().flatMap { it.contributionList }.flatMap { it.participantEmails }
                    .toSet()
                    .sorted()
                    .map { Contributor(email = it) }.toList(),
            )
    }

    @Test
    fun canQueryContributorsThatArePlayers() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val saveContributionCommands = listOf(
            stubContributionInput().copy(participantEmails = players.map { it.email }.toSet()),
            stubContributionInput().copy(participantEmails = setOf(players.random().email)),
            stubContributionInput().copy(participantEmails = setOf(players.random().email)),
        ).map { SaveContributionCommand(party.id, listOf(it)) }
    }) {
        savePartyState(party, players, emptyList())
        saveContributionCommands.forEach { sdk().fire(it) }
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    contributors {
                        email()
                        details()
                    }
                }
            },
        )
    } verify { result ->
        result?.party?.contributors?.map { it.details?.data?.element }
            .assertIsEqualTo(
                players.sortedBy { it.email },
            )
    }

    @Test
    fun canQueryContributorsWithMoreThanOneEmailCollapseToOnePlayer() = asyncSetup(object {
        val party = stubPartyDetails()
        val additionalEmail1 = uuidString()
        val additionalEmail2 = uuidString()
        val player = stubPlayer().copy(additionalEmails = setOf(additionalEmail1, additionalEmail2))
        val saveContributionCommands = listOf(
            stubContributionInput().copy(participantEmails = setOf(player.email)),
            stubContributionInput().copy(participantEmails = setOf(additionalEmail1)),
            stubContributionInput().copy(participantEmails = setOf(additionalEmail2)),
        ).map { SaveContributionCommand(party.id, listOf(it)) }
    }) {
        savePartyState(party, listOf(player), emptyList())
        saveContributionCommands.forEach { sdk().fire(it) }
    } exercise {
        sdk().fire(graphQuery { party(party.id) { contributors { details() } } })
    } verify { result ->
        result?.party?.contributors?.map { it.details?.data?.element }
            .assertIsEqualTo(
                listOf(player),
            )
    }

    @Test
    fun canQueryContributorsThatArePlayersViaUnverifiedEmail() = asyncSetup(object {
        val party = stubPartyDetails()
        val unvalidatedEmail = uuidString()
        val player = stubPlayer().copy(additionalEmails = setOf(unvalidatedEmail))
        val saveContributionCommands = listOf(
            stubContributionInput().copy(participantEmails = setOf(unvalidatedEmail)),
        ).map { SaveContributionCommand(party.id, listOf(it)) }
    }) {
        savePartyState(party, listOf(player), emptyList())
        saveContributionCommands.forEach { sdk().fire(it) }
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    contributors {
                        email()
                        details()
                    }
                }
            },
        )
    } verify { result ->
        result?.party?.contributors?.map { it.details?.data?.element }
            .assertIsEqualTo(listOf(player))
    }

    @Test
    fun canQueryContributorsThatArePlayersViaUnverifiedEmailWithDifferentCasing() = asyncSetup(object {
        val party = stubPartyDetails()
        val unvalidatedEmail = uuidString()
        val player = stubPlayer().copy(additionalEmails = setOf(unvalidatedEmail))
        val saveContributionCommands = listOf(
            stubContributionInput().copy(participantEmails = setOf(unvalidatedEmail.uppercase())),
        ).map { SaveContributionCommand(party.id, listOf(it)) }
    }) {
        savePartyState(party, listOf(player), emptyList())
        saveContributionCommands.forEach { sdk().fire(it) }
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    contributors {
                        email()
                        details()
                    }
                }
            },
        )
    } verify { result ->
        result?.party?.contributors?.map { it.details?.data?.element }
            .assertIsEqualTo(listOf(player))
    }

    private fun stubSaveContributionCommand(partyId: PartyId) = SaveContributionCommand(
        partyId = partyId,
        contributionList = listOf(stubContributionInput()),
    )

    private fun stubContributionInput() = ContributionInput(
        contributionId = uuidString(),
        participantEmails = setOf(uuidString()),
        hash = uuidString(),
        dateTime = Clock.System.now().minus(Random.nextInt(60).minutes).roundToMillis(),
        ease = Random.nextInt(),
        story = uuidString(),
        link = uuidString(),
        semver = uuidString(),
        label = uuidString(),
        firstCommit = uuidString(),
        firstCommitDateTime = Clock.System.now().minus(Random.nextInt(34).minutes).roundToMillis(),
        integrationDateTime = Clock.System.now().minus(Random.nextInt(23).minutes).roundToMillis(),
        cycleTime = (2..140).random().minutes,
    )
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
)
