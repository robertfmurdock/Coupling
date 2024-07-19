package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionQueryParams
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class DynamoContributionRepositoryTest {

    @Test
    fun canSaveAndLoadContributionByPartyId() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val contributions = generateSequence { stubContribution() }
            .take(3)
            .toList()
        val partyContributions = partyId.with(elementList = contributions)
        val unrelatedContribution = stubContribution()
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        partyContributions.forEach { repository.save(it) }
        repository.save(stubPartyId().with(unrelatedContribution))
        repository.get(ContributionQueryParams(partyId = partyId, window = null, limit = null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.assertIsEqualTo(
            partyContributions
                .sortedByDescending { "${it.element.dateTime} ${it.element.id}" }
                .map {
                    Record(
                        data = it,
                        modifyingUserId = userEmail,
                        isDeleted = false,
                        timestamp = clock.now(),
                    )
                },
        )
    }

    @Test
    fun givenManyNullsCanStillSaveAndLoadContribution() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val contributions = listOf(
            stubContribution().copy(
                label = null,
                hash = null,
                ease = null,
                firstCommit = null,
                semver = null,
                story = null,
                link = null,
                dateTime = null,
            ),
        )
        val partyContributions = partyId.with(elementList = contributions)
        val unrelatedContribution = stubContribution()
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        partyContributions.forEach { repository.save(it) }
        repository.save(stubPartyId().with(unrelatedContribution))
        repository.get(ContributionQueryParams(partyId = partyId, window = null, limit = null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.assertIsEqualTo(
            partyContributions
                .map {
                    Record(
                        data = it,
                        modifyingUserId = userEmail,
                        isDeleted = false,
                        timestamp = clock.now(),
                    )
                },
        )
    }

    @Test
    fun loadCanFilterByDurationWindow() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val expectedContributions = generateSequence {
            stubContribution().copy(dateTime = clock.now())
        }
            .take(3)
            .toList()
        val window = 10.days
        val outsideContribution = stubContribution().copy(dateTime = clock.now() - (window + 2.minutes))
        val allContributions = (expectedContributions + outsideContribution).shuffled()
        val partyContributions = partyId.with(elementList = allContributions)
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        partyContributions.forEach { repository.save(it) }
        repository.get(ContributionQueryParams(partyId, window, null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.elements.assertIsEqualTo(
            expectedContributions.sortedByDescending { "${it.dateTime} ${it.id}" },
        )
    }

    @Test
    fun clearingContributionsWillMakeThemNoLongerRetrievable() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val contributions = generateSequence { stubContribution() }
            .take(40)
            .toList()
        val partyContributions = partyId.with(elementList = contributions)
        val unrelatedContribution = stubContribution()
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        partyContributions.forEach { repository.save(it) }
        repository.save(stubPartyId().with(unrelatedContribution))
        repository.deleteAll(partyId)
        repository.get(ContributionQueryParams(partyId, null, null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.assertIsEqualTo(emptyList())
    }
}
