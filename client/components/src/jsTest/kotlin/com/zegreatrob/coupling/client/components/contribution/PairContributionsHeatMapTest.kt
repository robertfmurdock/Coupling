package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test

class PairContributionsHeatMapTest {

    @Test
    fun whenDataIsNotAvailableWillShowIndication() = asyncSetup(object {
        val window = GqlContributionWindow.All
        val data = listOf(
            Pair(
                pairOf(stubPlayer()),
                ContributionReport(contributions = emptyList()),
            ),
        )
    }) exercise {
        render {
            PairContributionsHeatMap(data, window, 0)
        }
    } verify { result ->
        screen.findByText("No contributions available for this period.")
    }
}
