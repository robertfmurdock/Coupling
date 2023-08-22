package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.repository.validation.assertIsCloseToNow
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class SdkContributionTest {

    @Test
    fun canSaveAndQueryContributions() = asyncSetup(object {
        val party = stubPartyDetails()
        val saveContributionCommands = generateSequence {
            SaveContributionCommand(
                partyId = party.id,
                contributionId = uuidString(),
                participantEmails = listOf(uuidString(), uuidString(), uuidString()),
                hash = uuidString(),
                dateTime = Clock.System.now().minus(Random.nextInt(60).minutes).roundToMillis(),
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
}

private fun Instant.roundToMillis(): Instant = Instant.fromEpochMilliseconds(toEpochMilliseconds())

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
)
