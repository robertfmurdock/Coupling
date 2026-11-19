package com.zegreatrob.coupling.client.components.contribution

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
import kotlinx.browser.document
import kotlinx.coroutines.await
import web.html.HTMLElement
import kotlin.test.Test

class ContributionCardHeaderTest {

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
