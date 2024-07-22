package com.zegreatrob.coupling.server.repository.cache

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionQueryParams
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.contribution.ContributionRepository
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class CachedContributionRepositoryTest {

    @Test
    fun willCacheBasedOnWindow() = asyncSetup(object : ScopeMint() {
        val backingRepository = object : ContributionRepository {
            val spy = SpyData<ContributionQueryParams, List<PartyRecord<Contribution>>>()

            override suspend fun get(params: ContributionQueryParams) = spy.spyFunction(params)
                .also {
                    delay(100)
                }

            override suspend fun save(partyContributions: PartyElement<List<Contribution>>) =
                throw NotImplementedError()

            override suspend fun deleteAll(partyId: PartyId) =
                throw NotImplementedError()
        }
        val cacheRepository: ContributionRepository = CachedContributionRepository(backingRepository)
        val params = ContributionQueryParams(stubPartyId(), 10.minutes, 20)
    }) {
        backingRepository.spy.whenever(params, emptyList())
    } exercise {
        repeat(100) {
            exerciseScope.launch { cacheRepository.get(params) }
        }
    } verify {
        backingRepository.spy.callCount
            .assertIsEqualTo(1)
    }
}
