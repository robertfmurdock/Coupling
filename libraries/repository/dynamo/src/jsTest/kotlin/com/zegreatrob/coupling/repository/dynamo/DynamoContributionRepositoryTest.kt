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
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class DynamoContributionRepositoryTest {

    @Test
    fun canSaveAndLoadContributionByPartyId() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString().toNotBlankString().getOrThrow()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val contributions = generateSequence { stubContribution() }
            .take(3)
            .toList()
        val unrelatedContribution = stubContribution()
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        repository.save(partyId.with(element = contributions))
        repository.save(stubPartyId().with(element = listOf(unrelatedContribution)))
        repository.get(ContributionQueryParams(partyId = partyId, window = null, limit = null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.assertIsEqualTo(
            partyId.with(elementList = contributions)
                .sortedByDescending { "${it.element.integrationDateTime} ${it.element.id.value}" }
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
    fun contributionsAreOrderedByIntegrationDateTime() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString().toNotBlankString().getOrThrow()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val contributions = generateSequence { stubContribution() }
            .take(8)
            .toList()
        val unrelatedContribution = stubContribution()
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        repository.save(partyId.with(element = contributions))
        repository.save(stubPartyId().with(element = listOf(unrelatedContribution)))
        repository.get(ContributionQueryParams(partyId = partyId, window = null, limit = null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.assertIsEqualTo(
            partyId.with(elementList = contributions)
                .sortedByDescending { "${it.element.integrationDateTime} ${it.element.id.value}" }
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
    fun contributionsAreOrderedByDateTimeWhenNoIntegrationDateTimeIsAvailable() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString().toNotBlankString().getOrThrow()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val contributions = generateSequence { stubContribution() }
            .take(8)
            .map { it.copy(integrationDateTime = null) }
            .toList()
        val unrelatedContribution = stubContribution()
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        repository.save(partyId.with(element = contributions))
        repository.save(stubPartyId().with(element = listOf(unrelatedContribution)))
        repository.get(ContributionQueryParams(partyId = partyId, window = null, limit = null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.assertIsEqualTo(
            partyId.with(elementList = contributions)
                .sortedByDescending { "${it.element.dateTime} ${it.element.id.value}" }
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
    fun canSaveOverContributionWithSameIDWithDifferentValues() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString().toNotBlankString().getOrThrow()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val contribution1 = stubContribution()
        val contributionUpdated = stubContribution().copy(id = contribution1.id)
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
        repository.save(partyId.with(element = listOf(contribution1)))
        repository.save(partyId.with(element = listOf(contributionUpdated)))
    } exercise {
        repository.get(ContributionQueryParams(partyId = partyId, window = null, limit = null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.elements.assertIsEqualTo(listOf(contributionUpdated))
    }

    @Test
    fun givenManyNullsCanStillSaveAndLoadContribution() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString().toNotBlankString().getOrThrow()
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
        repository.save(partyId.with(element = contributions))
        repository.save(stubPartyId().with(element = listOf(unrelatedContribution)))
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
    fun loadCanFilterByDurationWindowBasedOnIntegrationDateTime() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString().toNotBlankString().getOrThrow()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val expectedContributions = generateSequence {
            stubContribution().copy(integrationDateTime = clock.now())
        }
            .take(8)
            .toList()
        val window = 10.days
        val outsideContribution = stubContribution().copy(integrationDateTime = clock.now() - (window + 2.minutes))
        val allContributions = (expectedContributions + outsideContribution).shuffled()
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        repository.save(partyId.with(element = allContributions))
        repository.get(ContributionQueryParams(partyId, window, null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.elements.size.assertIsEqualTo(expectedContributions.size)

        result.elements.assertIsEqualTo(
            expectedContributions.sortedByDescending { "${it.integrationDateTime} ${it.id.value}" },
        )
    }

    @Test
    fun loadCanFilterByDurationWindowBasedOnDateTimeWhenNoIntegrationDateTimeAvailable() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString().toNotBlankString().getOrThrow()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val expectedContributions = generateSequence {
            stubContribution().copy(integrationDateTime = null, dateTime = clock.now())
        }
            .take(8)
            .toList()
        val window = 10.days
        val outsideContribution = stubContribution().copy(
            integrationDateTime = null,
            dateTime = clock.now() - (window + 2.minutes),
        )
        val allContributions = (expectedContributions + outsideContribution).shuffled()
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        repository.save(partyId.with(element = allContributions))
        repository.get(ContributionQueryParams(partyId, window, null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.elements.size.assertIsEqualTo(expectedContributions.size)
        result.elements.assertIsEqualTo(
            expectedContributions.sortedByDescending { "${it.dateTime} ${it.id.value}" },
        )
    }

    @Test
    fun clearingContributionsWillMakeThemNoLongerRetrievable() = asyncSetup(object {
        val partyId = stubPartyId()
        lateinit var repository: DynamoContributionRepository
        val userEmail = uuidString().toNotBlankString().getOrThrow()
        val clock = MagicClock().apply { currentTime = Clock.System.now() }
        val contributions = generateSequence { stubContribution() }
            .take(40)
            .toList()
        val unrelatedContribution = stubContribution()
    }) {
        repository = DynamoContributionRepository.invoke(userEmail, clock)
    } exercise {
        repository.save(partyId.with(element = contributions))
        repository.save(stubPartyId().with(element = listOf(unrelatedContribution)))
        repository.deleteAll(partyId)
        repository.get(ContributionQueryParams(partyId, null, null))
    } verify { result: List<PartyRecord<Contribution>> ->
        result.assertIsEqualTo(emptyList())
    }
}
