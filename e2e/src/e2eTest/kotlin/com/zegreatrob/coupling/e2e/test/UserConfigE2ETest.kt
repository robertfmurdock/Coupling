package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class UserConfigE2ETest {

    @Test
    fun loadingWillShowBasicInformationAboutTheUser() = e2eSetup(object {
        val page = UserPage
    }) exercise {
        page.goTo()
    } verify {
        page.element()
            .text()
            .contains(primaryAuthorizedUsername)
            .assertIsEqualTo(true)
    }
}
