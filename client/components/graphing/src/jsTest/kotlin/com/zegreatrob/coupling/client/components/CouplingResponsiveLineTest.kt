package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test

class CouplingResponsiveLineTest {

    @Test
    fun willNotExplodeWithNoData() = asyncSetup(object : ScopeMint() {
    }) exercise {
        render {
            CouplingResponsiveLine {
                data = emptyArray()
            }
        }
    } verify { result ->
        screen.getByTestId("coupling-responsive-line")
            .assertIsNotEqualTo(null)
    }
}
