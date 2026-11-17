package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.demo.returnToCouplingButton
import com.zegreatrob.coupling.client.party.AboutButton
import com.zegreatrob.coupling.client.party.NewPartyButton
import com.zegreatrob.coupling.client.pin.PinList
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import js.objects.unsafeJso
import kotlinx.coroutines.await
import react.ChildrenBuilder
import react.Fragment
import react.create
import react.router.RouteObject
import react.router.RouterProvider
import react.router.createMemoryRouter
import kotlin.test.AfterTest
import kotlin.test.Test

class ClientNavigationTest {

    @AfterTest
    fun cleanUp() = cleanup()

    @Test
    fun pageNavigationHasOneNamedLinkPerDestination() = asyncSetup(object {
        val expectedParty = PartyDetails(PartyId("keyboard-party"))
        val destinations = mapOf(
            "About" to "/about",
            "Form a new party!" to "/new-party/",
            "Add a new pin." to "/keyboard-party/pin/new",
        )
    }) exercise {
        renderNavigation {
            AboutButton()
            NewPartyButton()
            PinList {
                party = expectedParty
                pins = emptyList()
            }
        }
    } verify {
        destinations.forEach { (name, destination) ->
            val link = screen.getByRole("link", RoleOptions(name = name))
            link.getAttribute("href").assertIsEqualTo(destination)
            link.getAttribute("tabindex").assertIsEqualTo(null)
            link.querySelector("button, a, input, select, textarea, [tabindex]").assertIsEqualTo(null)
        }
    }

    @Test
    fun aboutBackNavigationHasOneNamedLink() = asyncSetup() exercise {
        renderNavigation { aboutPageContent {} }
    } verify {
        val link = screen.getByRole("link", RoleOptions(name = "Back to Coupling!"))
        link.getAttribute("href").assertIsEqualTo("/parties")
        link.querySelector("button").assertIsEqualTo(null)
    }

    @Test
    fun demoReturnNavigationHasOneNamedLink() = asyncSetup() exercise {
        renderNavigation { returnToCouplingButton() }
    } verify {
        val link = screen.getByRole("link", RoleOptions(name = "Back to Coupling!"))
        link.getAttribute("href").assertIsEqualTo("/parties")
        link.querySelector("button").assertIsEqualTo(null)
        link.querySelector("img")?.getAttribute("alt").assertIsEqualTo("")
    }

    @Test
    fun tabAndEnterOnAboutBackLinkNavigateToParties() = asyncSetup(object {
        val actor = userEvent.setup()
        val expectedRouter = navigationRouter { aboutPageContent {} }
    }) {
        render(RouterProvider.create { router = expectedRouter })
    } exercise {
        actor.tab().await()
        actor.keyboard("{Enter}").await()
    } verify {
        expectedRouter.state.location.pathname.assertIsEqualTo("/parties")
    }
}

internal fun renderNavigation(content: ChildrenBuilder.() -> Unit) = render(
    RouterProvider.create { router = navigationRouter(content) },
)

private fun navigationRouter(content: ChildrenBuilder.() -> Unit) = createMemoryRouter(
    arrayOf(
        unsafeJso<RouteObject> {
            path = "*"
            element = Fragment.create(content)
        },
    ),
)
