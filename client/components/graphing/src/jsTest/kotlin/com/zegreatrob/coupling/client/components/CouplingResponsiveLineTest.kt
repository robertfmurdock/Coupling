package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import react.ReactNode
import react.Suspense
import kotlin.test.Test
import react.ReactNode
import react.Suspense

@ExperimentalWasmJsInterop
class CouplingResponsiveLineTest {

    @Test
    fun willNotExplodeWithNoData() = asyncSetup(object {
    }) exercise {
        render {
            Suspense {
                fallback = ReactNode("Loading CouplingResponsiveLine")
                CouplingResponsiveLine {
                    data = emptyArray()
                }
            }
        }
    } verify { result ->
        screen.findByTestId("coupling-responsive-line")
            .assertIsNotEqualTo(null)
    }
}
