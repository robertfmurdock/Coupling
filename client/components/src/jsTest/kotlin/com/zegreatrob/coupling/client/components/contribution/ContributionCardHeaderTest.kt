package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.ContributionPopUpMenu
import com.zegreatrob.coupling.client.components.MemoryRouter
import com.zegreatrob.coupling.client.components.StubDispatcher
import com.zegreatrob.coupling.client.components.create
import com.zegreatrob.coupling.client.components.userEvent
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import kotlinx.browser.document
import kotlinx.coroutines.await
import popper.core.ReferenceElement
import web.html.HTMLElement
import kotlin.test.Test

class ContributionCardHeaderTest {

    @Test
    fun unknownParticipantKeepsItsAnchorAcrossPopupOpenAndDismiss() = asyncSetup(object {
        val expectedPartyId = stubPartyId()
        val expectedContribution = stubContribution().copy(
            participantEmails = setOf("alex@example.invalid", "sam@example.invalid"),
            link = null,
        )
        val actor = userEvent.setup()
        lateinit var originalTrigger: org.w3c.dom.HTMLElement
    }) {
        render(
            ContributionPopUpMenu.create(
                partyId = expectedPartyId,
                players = emptyList(),
                dispatchFunc = StubDispatcher().func(),
            ) { selectPlayer ->
                ContributionCardHeader {
                    partyId = expectedPartyId
                    contribution = expectedContribution
                    players = emptyList()
                    onPlayerClick = { player, element -> selectPlayer(ReferenceElement(element)!!, player) }
                }
            },
            RenderOptions(wrapper = MemoryRouter),
        )
        originalTrigger = screen.getByRole("button", RoleOptions(name = "alex"))
    } exercise {
        actor.click(originalTrigger).await()
        val openedTrigger = screen.getByRole("button", RoleOptions(name = "alex"))
        actor.keyboard("{Escape}").await()
        openedTrigger
    } verify { openedTrigger ->
        openedTrigger.assertIsEqualTo(originalTrigger, "Opening must preserve the popup anchor")
        originalTrigger.isConnected.assertIsEqualTo(true)
        document.activeElement.assertIsEqualTo(originalTrigger)
        screen.queryByRole("dialog").assertIsEqualTo(null)
    }

    @Test
    fun participantSupportsTabEnterAndSpaceWithItsVisibleName() = asyncSetup(object {
        val player = stubPlayer().copy(name = "Alex")
        val other = stubPlayer().copy(name = "Sam")
        val selected = mutableListOf<Pair<Player, HTMLElement>>()
        val actor = userEvent.setup()
    }) {
        render {
            ContributionCardHeader {
                partyId = stubPartyId()
                contribution = stubContribution().copy(participantEmails = setOf(player.email, other.email), link = null)
                players = listOf(player, other)
                onPlayerClick = { player, element -> selected.add(player to element) }
            }
        }
    } exercise {
        actor.tab().await()
        actor.keyboard("{Enter}").await()
        actor.keyboard(" ").await()
    } verify {
        val trigger = screen.getByRole("button", RoleOptions(name = "Alex"))
        document.activeElement.assertIsEqualTo(trigger)
        selected.map { it.first }.assertIsEqualTo(listOf(player, player))
        selected.map { it.second }.assertIsEqualTo(listOf(trigger, trigger))
        trigger.getAttribute("type").assertIsEqualTo("button")
        trigger.querySelector("a, button, input, select, textarea, [tabindex]").assertIsEqualTo(null)
    }

    @Test
    fun participantWithoutActionIsStatic() = setup(object {
        val player = stubPlayer().copy(name = "Alex")
        val other = stubPlayer().copy(name = "Sam")
    }) exercise {
        render {
            ContributionCardHeader {
                partyId = stubPartyId()
                contribution = stubContribution().copy(participantEmails = setOf(player.email, other.email), link = null)
                players = listOf(player, other)
            }
        }
    } verify {
        val name = screen.getByText("Alex")
        name.closest("a, button, [tabindex]").assertIsEqualTo(null)
        screen.queryByRole("button").assertIsEqualTo(null)
    }
}
