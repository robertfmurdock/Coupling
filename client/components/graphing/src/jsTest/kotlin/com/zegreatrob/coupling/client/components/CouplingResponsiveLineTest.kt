package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test

@ExperimentalWasmJsInterop
class CouplingResponsiveLineTest {

    @Test
    fun willNotExplodeWithNoData() = asyncSetup(object {
    }) exercise {
        render {
            CouplingResponsiveLine {
                data = emptyArray()
            }
        }
    } verify { result ->
        screen.findByTestId("coupling-responsive-line")
            .assertIsNotEqualTo(null)
    }
}
