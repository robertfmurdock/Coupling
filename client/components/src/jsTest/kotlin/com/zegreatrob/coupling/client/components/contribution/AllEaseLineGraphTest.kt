package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test

class AllEaseLineGraphTest {

    @Test
    fun whenDataIsNotAvailableWillShowIndication() = asyncSetup(object : ScopeMint() {
        val window = ContributionWindow.All
        val data = listOf(stubContribution().copy(ease = null))
    }) exercise {
        render {
            AllEaseLineGraph(data, window)
        }
    } verify {
        screen.findByText("No ease data available for this period.")
    }
}
