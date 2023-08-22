package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.datetime.Clock
import kotlin.test.Test

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
        repository.get(partyId)
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
}
