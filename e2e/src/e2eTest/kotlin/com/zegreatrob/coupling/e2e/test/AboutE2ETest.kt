package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class AboutE2ETest {
    @Test
    fun aboutPageWillRenderSuccessfully() = e2eSetup(object {
        val page = AboutPage
    }).exercise {
        page.goTo()
    } verify {
        page.header().text().apply {
            contains("About This App")
                .assertIsEqualTo(true, "About page did not contain title. See $it")
        }
    }
}
