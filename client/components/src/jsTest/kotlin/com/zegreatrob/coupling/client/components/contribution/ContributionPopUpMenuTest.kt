package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.ContributionPopUpMenu
import com.zegreatrob.coupling.client.components.MemoryRouter
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.create
import com.zegreatrob.coupling.client.components.userEvent
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
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
import react.ChildrenBuilder
import react.Key
import react.dom.html.ReactHTML.button
import kotlin.test.Test

class ContributionPopUpMenuTest {

    @Test
    fun openingPopupNamesTheDialogAndFocusesClose() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
    }) {
        render(popupWithParticipant(partyId, targetPlayer), RenderOptions(wrapper = MemoryRouter))
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Participant"))).await()
    } verify {
        screen.getByRole("button", RoleOptions(name = "Create Player")).textContent.assertIsEqualTo("Create Player")
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
            popupWithParticipant(partyId, targetPlayer, players = listOf(targetPlayer)),
            RenderOptions(wrapper = MemoryRouter),
        )
        actor.click(screen.getByRole("button", RoleOptions(name = "Participant"))).await()
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
            popupWithParticipant(partyId, targetPlayer, players = listOf(targetPlayer)),
            RenderOptions(wrapper = MemoryRouter),
        )
        originalTrigger = screen.getByRole("button", RoleOptions(name = "Participant"))
        actor.click(originalTrigger).await()
    } exercise {
        rendered.rerender(popupWithParticipant(partyId, targetPlayer, players = emptyList()))
    } verify {
        screen.getByRole("button", RoleOptions(name = "Participant")).assertIsEqualTo(originalTrigger, "Trigger survives rerender")
        screen.queryByRole("dialog").assertIsEqualTo(null)
        document.activeElement.assertIsEqualTo(originalTrigger)
    }

    @Test
    fun closeButtonDismissesPopupAndReturnsFocus() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
    }) {
        render(popupWithParticipant(partyId, targetPlayer), RenderOptions(wrapper = MemoryRouter))
        actor.click(screen.getByRole("button", RoleOptions(name = "Participant"))).await()
    } exercise {
        actor.click(screen.getByRole("button", RoleOptions(name = "Close"))).await()
    } verify {
        screen.queryByText("Create Player").assertIsEqualTo(null, "Pop up should have closed.")
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Participant")))
    }

    @Test
    fun escapeClosesPopupAndReturnsFocusToTrigger() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
    }) {
        render(popupWithParticipant(partyId, targetPlayer), RenderOptions(wrapper = MemoryRouter))
        actor.click(screen.getByRole("button", RoleOptions(name = "Participant"))).await()
    } exercise {
        actor.keyboard("{Escape}").await()
    } verify {
        screen.queryByText("Create Player").assertIsEqualTo(null)
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "Participant")))
    }

    @Test
    fun tabCanLeavePopupWithoutFocusBeingPulledBack() = asyncSetup(object {
        val partyId = stubPartyId()
        val targetPlayer = stubPlayer()
        val actor = userEvent.setup()
    }) {
        render(
            popupWithParticipant(partyId, targetPlayer) { button { +"After popup" } },
            RenderOptions(wrapper = MemoryRouter),
        )
        actor.click(screen.getByRole("button", RoleOptions(name = "Participant"))).await()
    } exercise {
        repeat(3) { actor.tab().await() }
    } verify {
        document.activeElement.assertIsEqualTo(screen.getByRole("button", RoleOptions(name = "After popup")))
        screen.queryByText("Create Player").assertIsEqualTo(null)
    }
}

private fun popupWithParticipant(
    partyId: PartyId,
    participant: Player,
    players: List<Player> = emptyList(),
    afterTrigger: ChildrenBuilder.() -> Unit = {},
) = ContributionPopUpMenu.create(
    partyId = partyId,
    players = players,
    dispatchFunc = StubDispatcher().func(),
) { selectPlayer ->
    button {
        key = Key("participant-trigger")
        onClick = { event -> selectPlayer(ReferenceElement(event.currentTarget)!!, participant) }
        +"Participant"
    }
    afterTrigger()
}
