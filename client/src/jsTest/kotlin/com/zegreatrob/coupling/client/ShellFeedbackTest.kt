package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.demo.LoadingPage
import com.zegreatrob.coupling.client.routing.notFoundContent
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlin.test.AfterTest
import kotlin.test.Test

class ShellFeedbackTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun loadingExposesNamedIndeterminateProgress() = asyncSetup() exercise {
        renderNavigation { LoadingPage() }
    } verify {
        val progress = screen.getByRole("progressbar", RoleOptions(name = "Loading page"))
        progress.getAttribute("value").assertIsEqualTo(null)
    }

    @Test
    fun errorAnnouncesProblemAndProvidesSingleRecoveryLink() = asyncSetup() exercise {
        renderNavigation { notFoundContent() }
    } verify {
        screen.getByRole("alert", RoleOptions()).textContent.assertIsEqualTo("Looks like an error happened.")
        val recovery = screen.getByRole("link", RoleOptions(name = "Go back home"))
        recovery.getAttribute("href").assertIsEqualTo("/welcome")
        recovery.querySelector("button, a, [tabindex]").assertIsEqualTo(null)
    }
}
