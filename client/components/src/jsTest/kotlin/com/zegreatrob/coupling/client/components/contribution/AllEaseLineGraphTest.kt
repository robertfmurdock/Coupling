package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test

class AllEaseLineGraphTest {

    @Test
    fun whenDataIsNotAvailableWillShowIndication() = asyncSetup(object : ScopeMint() {
        val window = GqlContributionWindow.All
        val data = listOf(stubContribution().copy(ease = null))
    }) exercise {
        render {
            AllEaseLineGraph(data, window)
        }
    } verify { result ->
        screen.findByText("No ease data available for this period.")
    }
}
