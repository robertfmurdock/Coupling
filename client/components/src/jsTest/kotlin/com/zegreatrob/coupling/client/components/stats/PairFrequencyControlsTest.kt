package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.stats.Visualization.LineOverTime
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import react.ReactNode
import kotlin.test.Test

class PairFrequencyControlsTest {

    @Test
    fun givenNoSelectionsWillPassThroughEmptyContent() = asyncSetup(object {
        var pairs = listOf(
            pairOf(stubPlayer()) to listOf(stubContribution()),
        )
        val viewSpy = SpyData<VisualizationContext, ReactNode>()
            .apply { spyWillReturn(ReactNode("Mission Complete")) }
    }) exercise {
        render(PairFrequencyControls.create(pairs, viewSpy::spyFunction, JsonContributionWindow.All, {}))
    } verify {
        viewSpy.spyReceivedValues.last()
            .assertIsEqualTo(VisualizationContext(LineOverTime, emptyList()))
    }

    @Test
    fun selectingAPairWillPassThroughThatPairContributions() = asyncSetup(object {
        val expectedPair = pairOf(stubPlayer())
        val expectedContributions = listOf(stubContribution())
        var pairs = listOf(
            expectedPair to expectedContributions,
            pairOf(stubPlayer()) to listOf(stubContribution()),
        )
        val viewSpy = SpyData<VisualizationContext, ReactNode>()
            .apply { spyWillReturn(listOf("Pending", "Mission Complete").map(::ReactNode)) }
        val actor = UserEvent.setup()
    }) {
        render(PairFrequencyControls.create(pairs, viewSpy::spyFunction, JsonContributionWindow.All, {}))
    } exercise {
        actor.click(screen.findByRole("checkbox", RoleOptions(expectedPair.pairName)))
    } verify {
        viewSpy.spyReceivedValues.last()
            .assertIsEqualTo(VisualizationContext(LineOverTime, listOf(expectedPair to expectedContributions)))
    }
}
