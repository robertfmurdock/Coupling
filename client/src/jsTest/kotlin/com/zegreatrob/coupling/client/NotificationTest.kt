package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.MarkdownContent
import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.browser.localStorage
import kotlinx.coroutines.await
import react.create
import react.dom.html.ReactHTML.form
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class NotificationTest {
    private val originalContent = MarkdownContent.content
    private var originalLog: String? = null

    @BeforeTest
    fun prepareContent() {
        originalLog = localStorage.getItem("notification-log")
        localStorage.removeItem("notification-log")
        MarkdownContent.content = originalContent.copy(recentInfoMd = "## Recent changes\nUseful release information.")
    }

    @AfterTest
    fun cleanUp() {
        cleanup()
        MarkdownContent.content = originalContent
        originalLog?.let { localStorage.setItem("notification-log", it) }
            ?: localStorage.removeItem("notification-log")
    }

    @Test
    fun openingRecentInformationDoesNotSubmitEnclosingForm() = asyncSetup(object {
        var submissions = 0
        val actor = userEvent.setup()
    }) {
        render(
            form.create {
                onSubmit = {
                    it.preventDefault()
                    submissions += 1
                }
                NotificationButton()
            },
        )
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Recent information"))).await()
    } verify {
        submissions.assertIsEqualTo(0)
        screen.getByRole("dialog", RoleOptions()).textContent
            ?.contains("Useful release information.").assertIsEqualTo(true)
    }
}
