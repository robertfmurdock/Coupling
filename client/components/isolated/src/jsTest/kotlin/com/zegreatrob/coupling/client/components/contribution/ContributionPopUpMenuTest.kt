package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.ContributionPopUpMenu
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.client.components.assertNotNull
import com.zegreatrob.coupling.client.components.create
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import popper.core.ReferenceElement
import react.dom.html.ReactHTML.button
import kotlin.test.Test

class ContributionPopUpMenuTest {

    @Test
    fun selectingPlayerCardWillShowPopUp() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = UserEvent.setup()
    }) {
        render(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
            },
            RenderOptions(wrapper = TestRouter),
        )
    } exercise {
        actor.click(screen.findByRole("button", RoleOptions(name = "Press me")))
    } verify {
        screen.findByText("Create Player")
            .assertNotNull()
    }

    @Test
    fun changingPlayersListWillClosePopup() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = UserEvent.setup()
    }) {
    } exercise {
        val result = render(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = listOf(targetPlayer),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
            },
            RenderOptions(wrapper = TestRouter),
        )
        actor.click(screen.findByRole("button", RoleOptions(name = "Press me")))
        result.rerender(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer -> +"lol" },
        )
    } verify {
        screen.queryByText("Create Player")
            .assertIsEqualTo(null, "Pop up should have closed.")
    }

    @Test
    fun clickingCloseWillHidePopUp() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = UserEvent.setup()
    }) {
        render(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
            },
            RenderOptions(wrapper = TestRouter),
        )
        actor.click(screen.findByRole("button", RoleOptions(name = "Press me")))
    } exercise {
        actor.click(screen.findByRole("button", RoleOptions(name = "Close")))
    } verify {
        screen.queryByText("Create Player")
            .assertIsEqualTo(null, "Pop up should have closed.")
    }
}
