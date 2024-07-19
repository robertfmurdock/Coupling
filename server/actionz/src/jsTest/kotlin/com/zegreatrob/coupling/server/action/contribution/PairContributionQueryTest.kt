package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours

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
        perform(PairContributionQuery(partyId, pair, null))
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

    @Test
    fun willNotIncludeContributionsOlderThanWindowFromNow() = asyncSetup(object : PairContributionQuery.Dispatcher {
        val targetEmail = "awesome@lol.com"
        val pair = pairOf(stubPlayer().copy(email = targetEmail))
        val partyId = stubPartyId()
        val expectedContribution = stubContribution().copy(
            participantEmails = setOf(targetEmail),
            dateTime = Clock.System.now().minus(2.hours),
        )
        val contributions = listOf(
            stubContribution().copy(
                participantEmails = setOf(targetEmail),
                dateTime = Clock.System.now().minus(4.hours),
            ),
            expectedContribution,
            stubContribution().copy(
                participantEmails = setOf(targetEmail),
                dateTime = null,
            ),
        )
        override val contributionRepository = ContributionGet { contributions.map { partyRecord(partyId, it, "") } }
    }) exercise {
        perform(PairContributionQuery(partyId, pair, window = 3.hours))
    } verify { result ->
        result.elements.assertIsEqualTo(listOf(expectedContribution))
    }
}
