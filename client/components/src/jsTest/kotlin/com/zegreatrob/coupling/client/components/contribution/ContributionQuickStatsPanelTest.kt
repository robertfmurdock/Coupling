package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class ContributionQuickStatsPanelTest {
    @Test
    fun rendersReportValuesWithTheirLabels() = asyncSetup() exercise {
        render(
            ContributionQuickStatsPanel.create(
                contributionCount = 5,
                medianCycleTime = 12.minutes,
                cycleTimeCount = 3,
                contributorCount = 4,
            ),
        )
    } verify {
        summaryValue("Recent contributions").assertIsEqualTo("5")
        summaryValue("With cycle time").assertIsEqualTo("3")
        summaryValue("Median cycle time").assertIsEqualTo("12m")
        summaryValue("Contributors").assertIsEqualTo("4")
        screen.getByText("Based on the recent contributions shown here.").textContent
            .assertIsEqualTo("Based on the recent contributions shown here.")
    }

    @Test
    fun missingCycleTimeDoesNotHideContributionOrContributorCounts() = asyncSetup() exercise {
        render(
            ContributionQuickStatsPanel.create(
                contributionCount = 2,
                medianCycleTime = null,
                cycleTimeCount = 0,
                contributorCount = 3,
            ),
        )
    } verify {
        summaryValue("Recent contributions").assertIsEqualTo("2")
        summaryValue("With cycle time").assertIsEqualTo("0")
        summaryValue("Median cycle time").assertIsEqualTo("Unavailable")
        summaryValue("Contributors").assertIsEqualTo("3")
    }

    @Test
    fun emptyReportShowsZeroCountsAndUnavailableMedian() = asyncSetup() exercise {
        render(
            ContributionQuickStatsPanel.create(
                contributionCount = 0,
                medianCycleTime = null,
                cycleTimeCount = 0,
                contributorCount = 0,
            ),
        )
    } verify {
        summaryValue("Recent contributions").assertIsEqualTo("0")
        summaryValue("With cycle time").assertIsEqualTo("0")
        summaryValue("Median cycle time").assertIsEqualTo("Unavailable")
        summaryValue("Contributors").assertIsEqualTo("0")
    }
}

private fun summaryValue(label: String) = screen.getByText(label).parentElement?.querySelector("dd")?.textContent
