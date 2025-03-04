package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.pairassignments.assertNotNull
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotlinx.browser.localStorage
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

class ReminderTest {

    @Test
    fun willShowReminderWhenLocalStorageHasNoEntries() = asyncSetup(object {
        val reminderId = uuidString()
    }) exercise {
        render { Reminder(id = reminderId) { +"The Content" } }
    } verify {
        screen.queryByText("The Content")
            .assertNotNull()
    }

    @Test
    fun willNotShowReminderWhenLocalStorageHasRecentEntry() = asyncSetup(object {
        val reminderId = uuidString()
    }) {
        localStorage.setItem("reminder-$reminderId", Clock.System.now().toString())
    } exercise {
        render { Reminder(id = reminderId) { +"The Content" } }
    } verify {
        screen.queryByText("The Content")
            .assertIsEqualTo(null)
    }

    @Test
    fun willShowReminderWhenLocalStorageHasEntryFromOverTwoWeeksAgo() = asyncSetup(object {
        val reminderId = uuidString()
    }) {
        val now = Clock.System.now()
        val twoWeeksAgo = now - 14.days - 1.milliseconds
        localStorage.setItem("reminder-$reminderId", twoWeeksAgo.toString())
    } exercise {
        render { Reminder(id = reminderId) { +"The Content" } }
    } verify {
        screen.queryByText("The Content")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun clickingCloseButtonWillUpdateLocalStorage() = asyncSetup(object {
        val reminderId = uuidString()
        val actor = UserEvent.setup()
    }) {
        render { Reminder(id = reminderId) { +"The Content" } }
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Close")))
    } verify {
        localStorage.getItem("reminder-$reminderId")
            ?.let { Instant.parse(it) }
            ?.let { Clock.System.now() - it < 300.milliseconds }
            .assertIsEqualTo(true, "should have written time close to now")
    }

    @Test
    fun clickingCloseButtonWillNoLongerShowContent() = asyncSetup(object {
        val reminderId = uuidString()
        val actor = UserEvent.setup()
    }) {
        render { Reminder(id = reminderId) { +"The Content" } }
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Close")))
    } verify {
        screen.queryByText("The Content")
            .assertIsEqualTo(null)
    }
}
