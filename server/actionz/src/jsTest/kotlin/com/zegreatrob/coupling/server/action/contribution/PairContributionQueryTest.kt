package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class PairContributionQueryTest {

    @Test
    fun willFindMatchUsingAdditionalEmails() = asyncSetup(object : PairContributionQuery.Dispatcher {
        val targetEmail = "awesome@lol.com"
        val pair = pairOf(
            stubPlayer().copy(
                email = "lol@lol.com",
                additionalEmails = setOf(targetEmail),
            ),
        )
        val partyId = stubPartyId()
        val expectedContribution =
            partyRecord(partyId, stubContribution().copy(participantEmails = setOf(targetEmail)), "")
        override val contributionRepository = ContributionGet { listOf(expectedContribution) }
    }) exercise {
        perform(PairContributionQuery(partyId, pair))
    } verify { result ->
        result.assertIsEqualTo(listOf(expectedContribution))
    }

    @Test
    fun willNotIncludeTwoPersonContributionsInSoloPair() = asyncSetup(object : PairContributionQuery.Dispatcher {
        val targetEmail = "awesome@lol.com"
        val notTargetEmail = "not.excellent@lol.com"
        val pair = pairOf(
            stubPlayer().copy(
                email = "lol@lol.com",
                additionalEmails = setOf(targetEmail),
            ),
        )
        val partyId = stubPartyId()
        val contribution =
            partyRecord(partyId, stubContribution().copy(participantEmails = setOf(targetEmail, notTargetEmail)), "")
        override val contributionRepository = ContributionGet { listOf(contribution) }
    }) exercise {
        perform(PairContributionQuery(partyId, pair))
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }
}
