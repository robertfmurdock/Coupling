package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotools.types.text.toNotBlankString
import react.ReactNode
import kotlin.test.Test

class PairFrequencyControlsTest {

    @Test
    fun givenNoSelectionsWillPassThroughEmptyContent() = asyncSetup(object {
        var pairs = listOf(
            pairOf(stubPlayer()) to stubContributionReport(listOf(stubContribution())),
        )
        val viewSpy = SpyData<VisualizationContext, ReactNode>()
            .apply { spyWillReturn(ReactNode("Mission Complete")) }
    }) exercise {
        render(PairFrequencyControls.create(pairs, ContributionWindow.All, {}) { +viewSpy.spyFunction(it) })
    } verify {
        viewSpy.spyReceivedValues.last()
            .assertIsEqualTo(VisualizationContext(Visualization.PairFrequencyHeatmap, emptyList()))
    }

    @Test
    fun selectingAPairWillPassThroughThatPairContributions() = asyncSetup(object {
        val expectedPair = pairOf(stubPlayer())
        val expectedContributions = listOf(stubContribution())
        val expectedReport = stubContributionReport(expectedContributions)
        var pairs = listOf(
            expectedPair to expectedReport,
            pairOf(stubPlayer()) to stubContributionReport(listOf(stubContribution())),
        )
        val viewSpy = SpyData<VisualizationContext, ReactNode>()
            .apply { spyWillReturn(listOf("Pending", "Mission Complete").map(::ReactNode)) }
        val actor = UserEvent.setup()
    }) {
        render(PairFrequencyControls.create(pairs, ContributionWindow.All, {}) { +viewSpy.spyFunction(it) })
    } exercise {
        actor.click(screen.findByRole("checkbox", RoleOptions(expectedPair.pairName)))
    } verify {
        viewSpy.spyReceivedValues.last()
            .assertIsEqualTo(VisualizationContext(Visualization.PairFrequencyHeatmap, listOf(expectedPair to expectedReport)))
    }
}

fun stubContributionReport(contributions: List<Contribution>) = ContributionReport(
    partyId = stubPartyId(),
    count = 0,
    contributors = emptyList(),
    contributions = contributions.map {
        partyRecord(
            partyId = stubPartyId(),
            data = it,
            modifyingUserEmail = "-".toNotBlankString().getOrThrow(),
        )
    },
)
