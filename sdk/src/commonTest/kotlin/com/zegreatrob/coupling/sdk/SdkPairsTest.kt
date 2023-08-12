package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class SdkPairsTest {

    @Test
    fun willShowAllCurrentPairCombinations() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(4)
    }) {
        with(sdk()) {
            fire(SavePartyCommand(party))
            players.forEach {
                fire(SavePlayerCommand(party.id, it))
            }
        }
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { players() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.players?.map { it.data.element } }
            .assertIsEqualTo(
                listOf(
                    listOf(players[0], players[1]),
                    listOf(players[0], players[2]),
                    listOf(players[0], players[3]),
                    listOf(players[1], players[2]),
                    listOf(players[1], players[3]),
                    listOf(players[2], players[3]),
                ),
            )
    }

    @Test
    fun willCountNumberOfTimesPairHasOccurred() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val pairAssignmentDocs = listOf(
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[1], players[2]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[2]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
        )
    }) {
        with(sdk()) {
            fire(SavePartyCommand(party))
            players.forEach {
                fire(SavePlayerCommand(party.id, it))
            }
            pairAssignmentDocs.forEach {
                fire(SavePairAssignmentsCommand(party.id, it))
            }
        }
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { count() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.count }
            .assertIsEqualTo(
                listOf(3, 1, 1),
            )
    }
}
