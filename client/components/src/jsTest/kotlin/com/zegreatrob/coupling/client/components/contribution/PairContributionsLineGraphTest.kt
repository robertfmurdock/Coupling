package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotools.types.text.toNotBlankString
import kotlin.test.Test

class PairContributionsLineGraphTest {

    @Test
    fun whenDataIsNotAvailableWillShowIndication() = asyncSetup(object : ScopeMint() {
        val window = ContributionWindow.All
        val data = listOf(
            Pair(
                pairOf(stubPlayer()),
                ContributionReport(
                    partyId = stubPartyId(),
                    count = 0,
                    contributions = listOf(
                        partyRecord(
                            stubPartyId(),
                            modifyingUserEmail = "-".toNotBlankString().getOrThrow(),
                            data = stubContribution().copy(dateTime = null),
                        ),
                    ),
                ),
            ),
        )
    }) exercise {
        render {
            PairContributionsLineGraph(data, window)
        }
    } verify {
        screen.findByText("No contributions with time data available for this period.")
    }
}
