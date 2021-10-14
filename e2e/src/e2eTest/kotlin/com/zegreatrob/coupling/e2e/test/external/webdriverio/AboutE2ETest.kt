package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

class AboutE2ETest {
    @Test
    fun aboutPageWillRenderSuccessfully() = e2eSetup(object {
        val page = AboutPage
    }).exercise {
        page.goTo()
    } verify {
        page.element().text().apply {
            contains("About This App")
                .assertIsEqualTo(true, "About page did not contain title. See $it")
        }
    }
}
