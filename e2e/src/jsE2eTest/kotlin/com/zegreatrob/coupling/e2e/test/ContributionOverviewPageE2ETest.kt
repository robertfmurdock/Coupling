package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.stubmodel.stubContributionInput
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

@Suppress("unused")
class ContributionOverviewPageE2ETest {

    class Context(val pairAssignments: List<PairAssignmentDocument>) {
        val page = HistoryPage
    }

    @Test
    fun whenThereAreNoContributionsWillShowSetup() = e2eSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
    } exercise {
        ContributionOverviewPage.goTo(party.id)
    } verify {
        ContributionOverviewPage.setupInstructions().isDisplayed()
            .assertIsEqualTo(true)
    }

    @Test
    fun whenThereAreNoContributionsWillContent() = e2eSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
        sdk().fire(SaveContributionCommand(party.id, listOf(stubContributionInput())))
    } exercise {
        ContributionOverviewPage.goTo(party.id)
    } verify {
        ContributionOverviewPage.mostRecentHeader(1).isDisplayed()
            .assertIsEqualTo(true)
    }
}
