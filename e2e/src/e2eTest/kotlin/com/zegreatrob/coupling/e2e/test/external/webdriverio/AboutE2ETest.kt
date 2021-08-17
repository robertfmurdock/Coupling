package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

class AboutE2ETest {
    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToAuth0() = e2eSetup(AboutPage) {
    } exercise {
        goTo()
    } verify {
        this.element().text().apply {
            contains("About This App")
                .assertIsEqualTo(true, "About page did not contain title. See $it")
        }
    }

}
