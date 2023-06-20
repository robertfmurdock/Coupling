package com.zegreatrob.coupling.client.components.pairassignments

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.StubDispatchFunc
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import js.core.jso
import korlibs.time.DateTime
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import react.router.MemoryRouter
import kotlin.test.Test

class PairAssignmentsTest {

    private val party = PartyDetails(PartyId("Party"))

    @Test
    fun willShowInRosterAllPlayersNotInCurrentPairs() = asyncSetup(object {
        val fellow = Player(id = "3", name = "fellow", avatarType = null)
        val guy = Player(id = "2", name = "Guy", avatarType = null)

        val rigby = Player(id = "1", name = "rigby", avatarType = null)
        val nerd = Player(id = "4", name = "nerd", avatarType = null)
        val pantsmaster = Player(id = "5", name = "pantsmaster", avatarType = null)

        val players = listOf(rigby, guy, fellow, nerd, pantsmaster)

        var pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = DateTime.now(),
            pairs = listOf(
                pairOf(
                    Player(id = "0", name = "Tom", avatarType = null),
                    Player(
                        id = "z",
                        name = "Jerry",
                        avatarType = null,
                    ),
                ),
                pairOf(fellow, guy),
            ).withPins(),
        )
    }) exercise {
        render(
            PairAssignments(
                party,
                players,
                pairAssignments,
                { pairAssignments = it },
                controls = Controls(StubDispatchFunc()) {},
                message = CouplingSocketMessage("", emptySet(), null),
                allowSave = false,
            ).create(),
            jso { wrapper = MemoryRouter },
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
                    .map(Player::id),
            )
    }

    @Test
    fun whenThereIsNoHistoryWillShowAllPlayersInRoster() = asyncSetup(object {
        val players = listOf(
            Player(id = "1", name = "rigby", avatarType = null),
            Player(id = "2", name = "Guy", avatarType = null),
            Player(id = "3", name = "fellow", avatarType = null),
            Player(id = "4", name = "nerd", avatarType = null),
            Player(id = "5", name = "pantsmaster", avatarType = null),
        )
    }) exercise {
        render(
            PairAssignments(
                party,
                players,
                null,
                {},
                controls = Controls(StubDispatchFunc()) {},
                message = CouplingSocketMessage("", emptySet(), null),
                allowSave = false,
            ).create(),
            jso { wrapper = MemoryRouter },
        )
    } verify {
        screen.findByText("Unpaired players")
            .parentElement
            ?.querySelectorAll("[data-player-id]")
            ?.asList()
            ?.mapNotNull { it as? HTMLElement }
            ?.map { it.getAttribute("data-player-id") }
            .assertIsEqualTo(players.map(Player::id))
    }
}
