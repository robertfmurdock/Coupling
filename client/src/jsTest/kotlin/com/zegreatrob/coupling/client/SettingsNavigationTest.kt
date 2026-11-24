package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.SettingsButton
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import js.objects.unsafeJso
import kotlinx.browser.document
import kotlinx.coroutines.await
import react.Fragment
import react.create
import react.dom.html.ReactHTML.button
import react.router.RouteObject
import react.router.RouterProvider
import react.router.createMemoryRouter
import kotlin.test.AfterTest
import kotlin.test.Test

class SettingsNavigationTest {
    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun settingsInitiallyExposesCollapsedState() = asyncSetup() exercise {
        renderSettings()
    } verify {
        screen.getByRole("button", RoleOptions(name = "Settings")).getAttribute("aria-expanded").assertIsEqualTo("false")
    }

    @Test
    fun keyboardOpeningExposesExpandedStateAndReachableDestinations() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        renderSettings()
        actor.tab().await()
    } exercise {
        actor.keyboard("{Enter}").await()
        (1..4).map {
            actor.tab().await()
            document.activeElement?.let { it.textContent to it.getAttribute("href") }
        }
    } verify { destinations ->
        destinations.assertIsEqualTo(
            listOf(
                "Party Settings" to "/settings-party/edit",
                "Secrets" to "/settings-party/secrets",
                "Contributions" to "/settings-party/contributions",
                "Developer settings" to "/settings-party/developer-settings/",
            ),
        )
        val settings = screen.getByRole("button", RoleOptions(name = "Settings"))
        settings.getAttribute("aria-expanded").assertIsEqualTo("true")
        document.getElementById(settings.getAttribute("aria-controls") ?: "")
            ?.contains(screen.getByRole("link", RoleOptions(name = "Party Settings"))).assertIsEqualTo(true)
    }

    @Test
    fun escapeFromSettingsLinkClosesDropdownAndReturnsFocus() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        renderSettings()
        actor.tab().await()
        actor.keyboard("{Enter}").await()
        actor.tab().await()
    } exercise {
        actor.keyboard("{Escape}").await()
    } verify {
        val settings = screen.getByRole("button", RoleOptions(name = "Settings"))
        settings.getAttribute("aria-expanded").assertIsEqualTo("false")
        document.activeElement.assertIsEqualTo(settings)
        document.querySelector("a[href='/settings-party/edit']").assertIsEqualTo(null)
    }

    @Test
    fun tabbingOutClosesSettingsWithoutStealingFocus() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        renderSettings()
        actor.tab().await()
        actor.keyboard("{Enter}").await()
    } exercise {
        repeat(5) { actor.tab().await() }
    } verify {
        screen.getByRole("button", RoleOptions(name = "Settings")).getAttribute("aria-expanded").assertIsEqualTo("false")
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "After settings")))
    }

    @Test
    fun pointerLeavingDoesNotRemoveFocusedSettingsLink() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        renderSettings()
        val settings = screen.getByRole("button", RoleOptions(name = "Settings"))
        actor.hover(settings).await()
        settings.focus()
        actor.tab().await()
    } exercise {
        actor.unhover(screen.getByRole("button", RoleOptions(name = "Settings"))).await()
    } verify {
        document.activeElement.assertIsEqualTo(screen.getByRole("link", RoleOptions(name = "Party Settings")))
        screen.getByRole("button", RoleOptions(name = "Settings")).getAttribute("aria-expanded").assertIsEqualTo("true")
    }

    @Test
    fun developerSettingsLinkOpensThePartyScopedPage() = asyncSetup(object {
        val actor = userEvent.setup()
        val expectedRouter = settingsDestinationRouter()
    }) {
        render(RouterProvider.create { router = expectedRouter })
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Settings"))).await()
        actor.click(screen.getByRole("link", RoleOptions(name = "Developer settings"))).await()
        waitFor { screen.getByRole("heading", RoleOptions(name = "Developer settings")) }.await()
        expectedRouter.state.location.pathname to screen.getByRole("heading", RoleOptions(name = "Developer settings")).textContent
    } verifyAnd { (path, heading) ->
        path.assertIsEqualTo("/settings-party/developer-settings/")
        heading.assertIsEqualTo("Developer settings")
    } teardown {
        expectedRouter.dispose()
    }
}

private fun renderSettings() = renderNavigation {
    SettingsButton { partyId = PartyId("settings-party") }
    button { +"After settings" }
}

private fun settingsDestinationRouter() = createMemoryRouter(
    arrayOf(
        unsafeJso<RouteObject> {
            path = "/settings-party"
            element = Fragment.create { SettingsButton { partyId = PartyId("settings-party") } }
        },
        unsafeJso<RouteObject> {
            path = "/:partyId/developer-settings/"
            element = DeveloperSettingsPage.create()
        },
    ),
    unsafeJso { initialEntries = arrayOf("/settings-party") },
)
