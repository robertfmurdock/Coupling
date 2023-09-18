package com.zegreatrob.coupling.client.components.pairassignments

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import js.core.jso
import kotlinx.datetime.Clock
import kotools.types.collection.notEmptyListOf
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import react.router.MemoryRouter
import kotlin.test.Test

class PairAssignmentsTest {

    private val party = PartyDetails(PartyId("Party"))

    @Test
    fun willShowInRosterAllPlayersNotInCurrentPairs() = asyncSetup(object {
        val fellow = defaultPlayer.copy(id = "3", name = "fellow")
        val guy = defaultPlayer.copy(id = "2", name = "Guy")

        val rigby = defaultPlayer.copy(id = "1", name = "rigby")
        val nerd = defaultPlayer.copy(id = "4", name = "nerd")
        val pantsmaster = defaultPlayer.copy(id = "5", name = "pantsmaster")

        val players = listOf(rigby, guy, fellow, nerd, pantsmaster)

        var pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = Clock.System.now(),
            pairs = notEmptyListOf(
                pairOf(
                    defaultPlayer.copy(id = "0", name = "Tom"),
                    defaultPlayer.copy(
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
            defaultPlayer.copy(id = "1", name = "rigby"),
            defaultPlayer.copy(id = "2", name = "Guy"),
            defaultPlayer.copy(id = "3", name = "fellow"),
            defaultPlayer.copy(id = "4", name = "nerd"),
            defaultPlayer.copy(id = "5", name = "pantsmaster"),
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
