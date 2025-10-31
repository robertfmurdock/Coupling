package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test

class StoryContributionGraphTest {
    @Test
    fun willRenderWithEmptyData() = asyncSetup(object {
    }) exercise {
        render {
            StoryContributionGraph(emptyList(), ContributionWindow.All, false)
        }
    } verify {
        screen.findByTestId("contribution-graph")
            .assertIsNotEqualTo(null)
    }
}
