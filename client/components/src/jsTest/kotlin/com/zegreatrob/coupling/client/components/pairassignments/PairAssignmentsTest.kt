package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.external.RenderOptions
import js.objects.unsafeJso
import kotools.types.collection.notEmptyListOf
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import kotlin.test.Test
import kotlin.time.Clock

class PairAssignmentsTest {

    private val party = PartyDetails(PartyId("Party"))

    @Test
    fun willShowInRosterAllPlayersNotInCurrentPairs() = asyncSetup(object {
        val fellow = stubPlayer().copy(name = "fellow")
        val guy = stubPlayer().copy(name = "Guy")

        val rigby = stubPlayer().copy(name = "rigby")
        val nerd = stubPlayer().copy(name = "nerd")
        val pantsmaster = stubPlayer().copy(name = "pantsmaster")

        val players = listOf(rigby, guy, fellow, nerd, pantsmaster)

        var pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId.new(),
            date = Clock.System.now(),
            pairs = notEmptyListOf(
                pairOf(
                    stubPlayer().copy(name = "Tom"),
                    stubPlayer().copy(name = "Jerry", avatarType = null),
                ),
                pairOf(fellow, guy),
            ).withPins(),
        )
    }) exercise {
        render(
            PairAssignments.create(
                party = party,
                boost = null,
                players = players,
                pairs = pairAssignments,
                setPairs = { pairAssignments = it },
                controls = Controls({ {} }) {},
                message = CouplingSocketMessage("", emptySet(), null),
                allowSave = false,
            ),
            RenderOptions(wrapper = TestRouter),
        )
    } verify {
        screen.findByText("Unpaired players")
            .parentElement
            ?.querySelectorAll("[data-player-id]")
            ?.asList()
            ?.mapNotNull { it as? HTMLElement }
            ?.map { it.getAttribute("data-player-id") }
            .assertIsEqualTo(
                listOf(rigby, nerd, pantsmaster)
                    .map(Player::id)
                    .map { it.value.toString() },
            )
    }

    @Test
    fun whenThereIsNoHistoryWillShowAllPlayersInRoster() = asyncSetup(object {
        val players = listOf(
            stubPlayer().copy(name = "rigby"),
            stubPlayer().copy(name = "Guy"),
            stubPlayer().copy(name = "fellow"),
            stubPlayer().copy(name = "nerd"),
            stubPlayer().copy(name = "pantsmaster"),
        )
    }) exercise {
        render(
            PairAssignments.create(
                party = party,
                boost = null,
                players = players,
                pairs = null,
                setPairs = {},
                controls = Controls({ { } }) {},
                message = CouplingSocketMessage("", emptySet(), null),
                allowSave = false,
            ),
            RenderOptions(wrapper = TestRouter),
        )
    } verify {
        screen.findByText("Unpaired players")
            .parentElement
            ?.querySelectorAll("[data-player-id]")
            ?.asList()
            ?.mapNotNull { it as? HTMLElement }
            ?.map { it.getAttribute("data-player-id") }
            .assertIsEqualTo(players.map(Player::id).map { it.value.toString() })
    }
}
