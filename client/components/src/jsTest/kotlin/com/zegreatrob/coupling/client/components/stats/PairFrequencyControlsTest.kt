package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import react.ReactNode
import kotlin.test.Test

class PairFrequencyControlsTest {

    @Test
    fun givenNoSelectionsWillPassThroughEmptyContent() = asyncSetup(object {
        var pairs = listOf(
            pairOf(stubPlayer()) to listOf(stubContribution()),
        )
        val viewSpy = SpyData<List<Pair<CouplingPair, List<Contribution>>>, ReactNode>()
            .apply { spyWillReturn(ReactNode("Mission Complete")) }
    }) exercise {
        render(PairFrequencyControls.create(pairs, viewSpy::spyFunction))
    } verify {
        viewSpy.spyReceivedValues.last()
            .assertIsEqualTo(emptyList())
    }
}
