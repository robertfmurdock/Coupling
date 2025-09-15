package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test

class AllContributionsLineGraphTest {

    @Test
    fun whenDataIsNotAvailableWillShowIndication() = asyncSetup(object : ScopeMint() {
        val window = GqlContributionWindow.All
        val data = listOf(stubContribution().copy(dateTime = null))
    }) exercise {
        render {
            AllContributionsLineGraph(data, window)
        }
    } verify { result ->
        screen.findByText("No contributions with time data available for this period.")
    }
}
