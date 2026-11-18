package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.MarkdownContent
import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.MouseEventInit
import react.create
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h1
import web.html.HTMLDialogElement
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
    fun notificationTriggerKeepsHeadingScaleWithoutNativeButtonBorder() = asyncSetup {
        render(h1.create { NotificationButton() })
    } exercise {
        window.getComputedStyle(screen.getByRole("button", RoleOptions(name = "Recent information")))
    } verify { style ->
        style.fontSize.assertIsEqualTo(window.getComputedStyle(screen.getByRole("heading", RoleOptions())).fontSize)
        style.borderTopWidth.assertIsEqualTo("0px")
    }

    @Test
    fun recentInformationStartsWithSuppliedNewsHeading() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(NotificationButton.create())
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Recent information"))).await()
    } verify {
        screen.getByRole("dialog", RoleOptions(name = "Recent information"))
            .querySelector("h1, h2, h3")?.textContent.assertIsEqualTo("Recent changes")
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

    @Test
    fun openingRecentInformationFocusesCloseControlInNamedModal() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(NotificationButton.create())
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Recent information"))).await()
    } verify {
        val dialog = screen.getByRole("dialog", RoleOptions(name = "Recent information"))
        dialog.matches(":modal").assertIsEqualTo(true)
        val close = screen.getByRole("button", RoleOptions(name = "Close"))
        dialog.contains(close).assertIsEqualTo(true)
        document.activeElement.assertIsEqualTo(close)
    }

    @Test
    fun closingRecentInformationReturnsFocusAndMarksNotesSeen() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(NotificationButton.create())
        actor.click(screen.getByRole("button", RoleOptions(name = "Recent information"))).await()
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Close"))).await()
    } verify {
        waitFor {
            document.querySelector(":modal").assertIsEqualTo(null)
            document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Recent information")))
            localStorage.getItem("notification-log").assertIsEqualTo("[\"## Recent changes\"]")
        }.await()
    }

    @Test
    fun platformCloseRequestReturnsFocusAndMarksNotesSeen() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(NotificationButton.create())
        actor.click(screen.getByRole("button", RoleOptions(name = "Recent information"))).await()
    } exercise {
        screen.getByRole("dialog", RoleOptions(name = "Recent information"))
            .unsafeCast<HTMLDialogElement>().requestClose()
    } verify {
        waitFor {
            document.querySelector(":modal").assertIsEqualTo(null)
            document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Recent information")))
            localStorage.getItem("notification-log").assertIsEqualTo("[\"## Recent changes\"]")
        }.await()
    }

    @Test
    fun backdropPressAndReleaseCloseRecentInformation() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(NotificationButton.create())
        actor.click(screen.getByRole("button", RoleOptions(name = "Recent information"))).await()
    } exercise {
        val dialog = screen.getByRole("dialog", RoleOptions(name = "Recent information"))
        dialog.pointerAt("pointerdown", -1, -1)
        dialog.pointerAt("pointerup", -1, -1)
    } verify {
        waitFor { document.querySelector(":modal").assertIsEqualTo(null) }.await()
    }

    @Test
    fun dialogPaddingPressDoesNotCloseRecentInformation() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(NotificationButton.create())
        actor.click(screen.getByRole("button", RoleOptions(name = "Recent information"))).await()
    } exercise {
        val dialog = screen.getByRole("dialog", RoleOptions(name = "Recent information"))
        val bounds = dialog.getBoundingClientRect()
        dialog.pointerAt("pointerdown", bounds.left.toInt() + 2, bounds.top.toInt() + 2)
        dialog.pointerAt("pointerup", bounds.left.toInt() + 2, bounds.top.toInt() + 2)
    } verify {
        screen.getByRole("dialog", RoleOptions(name = "Recent information")).matches(":modal").assertIsEqualTo(true)
    }

    @Test
    fun draggingFromDialogToBackdropDoesNotCloseRecentInformation() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        render(NotificationButton.create())
        actor.click(screen.getByRole("button", RoleOptions(name = "Recent information"))).await()
    } exercise {
        val dialog = screen.getByRole("dialog", RoleOptions(name = "Recent information"))
        val bounds = dialog.getBoundingClientRect()
        dialog.pointerAt("pointerdown", bounds.left.toInt() + 2, bounds.top.toInt() + 2)
        dialog.pointerAt("pointerup", -1, -1)
    } verify {
        screen.getByRole("dialog", RoleOptions(name = "Recent information")).matches(":modal").assertIsEqualTo(true)
    }
}

private fun HTMLElement.pointerAt(type: String, x: Int, y: Int) = dispatchEvent(
    MouseEvent(type, MouseEventInit(bubbles = true, clientX = x, clientY = y)),
)
