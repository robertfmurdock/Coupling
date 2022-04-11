package com.zegreatrob.coupling.client.pairassignments

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.player.playerRoster
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PairAssignmentsTest {

    val tribe = Party(PartyId("Party"))

    @Test
    fun willShowInRosterAllPlayersNotInCurrentPairs(): Unit = setup(object {
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
                pairOf(fellow, guy)
            ).withPins()
        )
    }) exercise {
        shallow(
            PairAssignments(
                tribe,
                players,
                pairAssignments,
                { pairAssignments = it },
                controls = Controls(StubDispatchFunc()) {},
                message = CouplingSocketMessage("", emptySet(), null),
                allowSave = false
            )
        )
    } verify { wrapper ->
        wrapper.find(playerRoster)
            .dataprops()
            .players
            .assertIsEqualTo(
                listOf(rigby, nerd, pantsmaster)
            )
    }

    @Test
    fun whenThereIsNoHistoryWillShowAllPlayersInRoster() = setup(object {
        val players = listOf(
            Player(id = "1", name = "rigby"),
            Player(id = "2", name = "Guy"),
            Player(id = "3", name = "fellow"),
            Player(id = "4", name = "nerd"),
            Player(id = "5", name = "pantsmaster")
        )
    }) exercise {
        shallow(
            PairAssignments(
                tribe,
                players,
                null,
                {},
                controls = Controls(StubDispatchFunc()) {},
                message = CouplingSocketMessage("", emptySet(), null),
                allowSave = false
            )
        )
    } verify { wrapper ->
        wrapper.find(playerRoster)
            .dataprops()
            .players
            .assertIsEqualTo(players)
    }

}
