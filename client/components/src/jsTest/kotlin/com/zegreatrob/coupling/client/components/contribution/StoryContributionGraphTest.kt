package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.components.client.contribution.StoryContributionGraph
import com.zegreatrob.coupling.json.GqlContributionWindow
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
            StoryContributionGraph(emptyList(), GqlContributionWindow.All, false)
        }
    } verify {
        screen.findByTestId("contribution-graph")
            .assertIsNotEqualTo(null)
    }
}
