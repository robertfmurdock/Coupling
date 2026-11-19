package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.ContributionPopUpMenu
import com.zegreatrob.coupling.client.components.MemoryRouter
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.assertNotNull
import com.zegreatrob.coupling.client.components.create
import com.zegreatrob.coupling.client.components.userEvent
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import com.zegreatrob.wrapper.testinglibrary.react.external.Result
import kotlinx.browser.document
import kotlinx.coroutines.await
import popper.core.ReferenceElement
import react.Key
import react.dom.html.ReactHTML.button
import kotlin.test.Test

class ContributionPopUpMenuTest {

    @Test
    fun selectingPlayerCardWillShowPopUp() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
    }) {
        render(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    key = Key("participant-trigger")
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
            },
            RenderOptions(wrapper = MemoryRouter),
        )
    } exercise {
        actor.click(screen.findByRole("button", RoleOptions(name = "Press me"))).await()
    } verify {
        screen.findByText("Create Player")
            .assertNotNull()
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Close")))
        screen.getByRole("dialog", RoleOptions(name = "Contributor actions for ${targetPlayer.name}"))
            .getAttribute("aria-modal").assertIsEqualTo("false")
    }

    @Test
    fun changingPlayersListClosesPopupWhenTriggerWasRemoved() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
        lateinit var rendered: Result
    }) {
        rendered = render(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = listOf(targetPlayer),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    key = Key("participant-trigger")
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
            },
            RenderOptions(wrapper = MemoryRouter),
        )
        actor.click(screen.findByRole("button", RoleOptions(name = "Press me"))).await()
    } exercise {
        rendered.rerender(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { +"Participants removed" },
        )
    } verify {
        screen.queryByText("Player Config").assertIsEqualTo(null)
        screen.queryByRole("dialog").assertIsEqualTo(null)
        document.activeElement.assertIsEqualTo(document.body)
    }

    @Test
    fun changingPlayersListReturnsFocusToSurvivingTrigger() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
        lateinit var rendered: Result
        lateinit var originalTrigger: org.w3c.dom.HTMLElement
    }) {
        rendered = render(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = listOf(targetPlayer),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    key = Key("participant-trigger")
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
            },
            RenderOptions(wrapper = MemoryRouter),
        )
        originalTrigger = screen.findByRole("button", RoleOptions(name = "Press me"))
        actor.click(originalTrigger).await()
    } exercise {
        rendered.rerender(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    key = Key("participant-trigger")
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
            },
        )
    } verify {
        originalTrigger.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Press me")), "Trigger survives rerender")
        screen.queryByRole("dialog").assertIsEqualTo(null)
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Press me")))
    }

    @Test
    fun clickingCloseWillHidePopUp() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
    }) {
        render(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    key = Key("participant-trigger")
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
            },
            RenderOptions(wrapper = MemoryRouter),
        )
        actor.click(screen.findByRole("button", RoleOptions(name = "Press me"))).await()
    } exercise {
        actor.click(screen.findByRole("button", RoleOptions(name = "Close"))).await()
    } verify {
        screen.queryByText("Create Player")
            .assertIsEqualTo(null, "Pop up should have closed.")
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Press me")))
    }

    @Test
    fun escapeClosesPopupAndReturnsFocusToTrigger() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
    }) {
        render(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    key = Key("participant-trigger")
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
            },
            RenderOptions(wrapper = MemoryRouter),
        )
        actor.click(screen.findByRole("button", RoleOptions(name = "Press me"))).await()
    } exercise {
        actor.keyboard("{Escape}").await()
    } verify {
        screen.queryByText("Create Player").assertIsEqualTo(null)
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Press me")))
    }

    @Test
    fun tabCanLeavePopupWithoutFocusBeingPulledBack() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
    }) {
        render(
            ContributionPopUpMenu.create(
                partyId = partyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { setSelectedPlayer ->
                button {
                    key = Key("participant-trigger")
                    onClick = { event -> setSelectedPlayer(ReferenceElement(event.currentTarget)!!, targetPlayer) }
                    +"Press me"
                }
                button { +"After popup" }
            },
            RenderOptions(wrapper = MemoryRouter),
        )
        actor.click(screen.findByRole("button", RoleOptions(name = "Press me"))).await()
    } exercise {
        repeat(3) { actor.tab().await() }
    } verify {
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "After popup")))
        screen.queryByText("Create Player").assertIsEqualTo(null)
    }
}
