package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.client.components.stats.stubContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test
import kotlin.time.Duration.Companion.nanoseconds

class PairCycleTimeBarChartTest {
    @Test
    fun canRenderSuccessfullyWithData() = asyncSetup(object {
        val data = listOf(
            Pair(
                listOf(stubPlayer(), stubPlayer()).toCouplingPair(),
                stubContributionReport(emptyList()).copy(medianCycleTime = 728000000000.nanoseconds),
            ),
            Pair(
                listOf(stubPlayer(), stubPlayer()).toCouplingPair(),
                stubContributionReport(emptyList()).copy(medianCycleTime = 729000000000.nanoseconds),
            ),
        )
    }) exercise {
        render(
            PairCycleTimeBarChart.create(
                data = data,
                window = ContributionWindow.All,
            ),
        )
    } verify {
        screen.findByTestId("pair-cycle-time-bar-chart")
            .assertIsNotEqualTo(null)
    }
}
