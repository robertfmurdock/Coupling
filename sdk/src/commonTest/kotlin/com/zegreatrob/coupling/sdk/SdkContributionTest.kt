package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.model.Contribution
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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SdkContributionTest {

    @Test
    fun canSaveAndQueryContributions() = asyncSetup(object {
        val party = stubPartyDetails()
        val saveContributionCommands = generateSequence {
            SaveContributionCommand(
                partyId = party.id,
                contributionId = uuidString(),
                participantEmails = setOf(uuidString(), uuidString(), uuidString()),
                hash = uuidString(),
                dateTime = Clock.System.now().minus(Random.nextInt(60 * 60).seconds).roundToMillis(),
                ease = Random.nextInt(),
                story = uuidString(),
                link = uuidString(),
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
                saveContributionCommands.toExpectedContributions(),
            )
        result?.party?.contributions?.elements?.map { it.createdAt }?.forEach { createdAt ->
            createdAt.assertIsCloseToNow()
        }
    }

    @Test
    fun canQueryContributionsByPair() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val contributionCommand = stubSaveContributionCommand(party.id)
            .copy(participantEmails = setOf(players[1].email))
        val saveContributionCommands = listOf(
            stubSaveContributionCommand(party.id),
            contributionCommand,
            stubSaveContributionCommand(party.id),
        )
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
            ?.find { it.players?.elements?.map(Player::id)?.contains(players[1].id) == true }
            ?.contributions?.elements?.withoutCreatedAt()
            .assertIsEqualTo(
                listOf(
                    contributionCommand.toExpectedContribution(),
                ),
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
        result?.party?.contributors
            .assertIsEqualTo(
                saveContributionCommands.flatMap { it.participantEmails }
                    .toSet()
                    .sorted()
                    .map { Contributor(it) },
            )
    }

    @Test
    fun canQueryContributorsThatArePlayers() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val saveContributionCommands = listOf(
            stubSaveContributionCommand(party.id).copy(participantEmails = players.map { it.email }.toSet()),
            stubSaveContributionCommand(party.id).copy(participantEmails = setOf(players.random().email)),
            stubSaveContributionCommand(party.id).copy(participantEmails = setOf(players.random().email)),
        )
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
            stubSaveContributionCommand(party.id).copy(participantEmails = setOf(player.email)),
            stubSaveContributionCommand(party.id).copy(participantEmails = setOf(additionalEmail1)),
            stubSaveContributionCommand(party.id).copy(participantEmails = setOf(additionalEmail2)),
        )
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
            stubSaveContributionCommand(party.id).copy(participantEmails = setOf(unvalidatedEmail)),
        )
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
            stubSaveContributionCommand(party.id).copy(participantEmails = setOf(unvalidatedEmail.uppercase())),
        )
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
        contributionId = uuidString(),
        participantEmails = setOf(uuidString()),
        hash = uuidString(),
        dateTime = Clock.System.now().minus(Random.nextInt(60).minutes).roundToMillis(),
        ease = Random.nextInt(),
        story = uuidString(),
        link = uuidString(),
        label = uuidString(),
        semver = uuidString(),
        firstCommit = uuidString(),
    )
}

private fun List<Contribution>.withoutCreatedAt(): List<Contribution> = map {
    it.copy(createdAt = Instant.DISTANT_PAST)
}

private fun List<SaveContributionCommand>.toExpectedContributions() = map { it.toExpectedContribution() }
    .sortedByDescending { it.dateTime }

private fun SaveContributionCommand.toExpectedContribution() = Contribution(
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
)
