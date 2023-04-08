package com.zegreatrob.coupling.client.pairassignments

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import react.router.MemoryRouter
import kotlin.js.json
import kotlin.test.Test

class PairAssignmentsTest {

    private val party = Party(PartyId("Party"))

    @Test
    fun willShowInRosterAllPlayersNotInCurrentPairs() = asyncSetup(object {
        val fellow = Player(id = "3", name = "fellow")
        val guy = Player(id = "2", name = "Guy")

        val rigby = Player(id = "1", name = "rigby")
        val nerd = Player(id = "4", name = "nerd")
        val pantsmaster = Player(id = "5", name = "pantsmaster")

        val players = listOf(rigby, guy, fellow, nerd, pantsmaster)

        var pairAssignments = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = DateTime.now(),
            pairs = listOf(
                pairOf(Player(id = "0", name = "Tom"), Player(id = "z", name = "Jerry")),
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
            json("wrapper" to MemoryRouter),
        )
    } verify {
        screen.findByText("Unpaired players")
            .await()
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
            Player(id = "1", name = "rigby"),
            Player(id = "2", name = "Guy"),
            Player(id = "3", name = "fellow"),
            Player(id = "4", name = "nerd"),
            Player(id = "5", name = "pantsmaster"),
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
            json("wrapper" to MemoryRouter),
        )
    } verify {
        screen.findByText("Unpaired players")
            .await()
            .parentElement
            ?.querySelectorAll("[data-player-id]")
            ?.asList()
            ?.mapNotNull { it as? HTMLElement }
            ?.map { it.getAttribute("data-player-id") }
            .assertIsEqualTo(players.map(Player::id))
    }
}
