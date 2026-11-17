package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.pairassignments.PrepareToSpinButton
import com.zegreatrob.coupling.client.components.pairassignments.viewHistoryButton
import com.zegreatrob.coupling.client.components.party.PartySelectButton
import com.zegreatrob.coupling.client.components.pin.pinListButton
import com.zegreatrob.coupling.client.components.player.AddPlayerButton
import com.zegreatrob.coupling.client.components.player.viewRetireesButton
import com.zegreatrob.coupling.client.components.stats.statisticsButton
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import kotlin.test.Test

class NavigationLinkTest {

    @Test
    fun partyNavigationHasOneInteractiveTargetPerDestination() = setup(object {
        val expectedParty = PartyDetails(PartyId("keyboard-party"))
        val destinations = mapOf(
            "Add Player!" to "/keyboard-party/player/new/",
            "Retirees!" to "/keyboard-party/players/retired",
            "Party select" to "/parties/",
            "Statistics!" to "/keyboard-party/statistics",
            "Pin Bag!" to "/keyboard-party/pins/",
            "History!" to "/keyboard-party/history/",
            "Sign Out" to "/logout",
            "Prepare to spin!" to "/keyboard-party/prepare/",
        )
    }) exercise {
        render(RenderOptions(wrapper = TestRouter)) {
            AddPlayerButton { partyId = expectedParty.id }
            viewRetireesButton(expectedParty)
            PartySelectButton()
            statisticsButton(expectedParty)
            pinListButton(expectedParty)
            viewHistoryButton(expectedParty)
            LogoutButton()
            PrepareToSpinButton { party = expectedParty }
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
    fun graphiqlNavigationHasOneNamedInteractiveTarget() = setup(Unit) exercise {
        render(RenderOptions(wrapper = TestRouter)) { GqlButton() }
    } verify {
        val link = screen.getByRole("link", RoleOptions(name = "GraphiQL"))
        link.getAttribute("href").assertIsEqualTo("/graphiql")
        link.getAttribute("tabindex").assertIsEqualTo(null)
        link.querySelector("button, a, input, select, textarea, [tabindex]").assertIsEqualTo(null)
    }

    @Test
    fun demoNavigationHasOneNamedInteractiveTarget() = setup(Unit) exercise {
        render(RenderOptions(wrapper = TestRouter)) { DemoButton() }
    } verify {
        val link = screen.getByRole("link", RoleOptions(name = "Demo"))
        link.getAttribute("href").assertIsEqualTo("/demo")
        link.getAttribute("tabindex").assertIsEqualTo(null)
        link.querySelector("button, a, input, select, textarea, [tabindex]").assertIsEqualTo(null)
    }
}
