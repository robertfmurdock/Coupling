package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import kotlinx.datetime.Clock
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.time.Duration.Companion.days

class SdkPairsTest {

    @Test
    fun willShowAllCurrentPairCombinations() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = stubPlayers(4)
    }) {
        savePartyState(party, players, emptyList())
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { players() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.players?.map(PartyRecord<Player>::data)?.map(PartyElement<Player>::element) }
            .assertIsEqualTo(
                listOf(
                    listOf(players[0], players[1]),
                    listOf(players[0], players[2]),
                    listOf(players[0], players[3]),
                    listOf(players[1], players[2]),
                    listOf(players[1], players[3]),
                    listOf(players[2], players[3]),
                ).plus(players.map { listOf(it) }),
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
        savePartyState(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { count() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.count }
            .assertIsEqualTo(
                listOf(3, 1, 1, 0, 0, 0),
            )
    }

    @Test
    fun willCalculatePairHeat() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val pairAssignmentDocs = listOf(
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins())),
        )
    }) {
        savePartyState(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { heat() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.heat }
            .assertIsEqualTo(
                listOf(7.0, 0.0, 0.0, null, null, null),
            )
    }

    @Test
    fun willShowSpinsSinceLastPaired() = asyncSetup(object : ScopeMint() {
        val party = stubPartyDetails()
        val players = stubPlayers(3)
        val now = Clock.System.now()
        val pairAssignmentDocs = listOf(
            stubPairAssignmentDoc().copy(
                date = now.minus(5.days),
                pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()),
            ),
            stubPairAssignmentDoc().copy(
                date = now.minus(4.days),
                pairs = notEmptyListOf(pairOf(players[1], players[2]).withPins()),
            ),
            stubPairAssignmentDoc().copy(
                date = now.minus(3.days),
                pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()),
            ),
            stubPairAssignmentDoc().copy(
                date = now.minus(2.days),
                pairs = notEmptyListOf(pairOf(players[0], players[2]).withPins()),
            ),
            stubPairAssignmentDoc().copy(
                date = now.minus(1.days),
                pairs = notEmptyListOf(pairOf(players[0], players[1]).withPins()),
            ),
        )
    }) {
        savePartyState(party, players, pairAssignmentDocs)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { spinsSinceLastPaired() } } })
    } verify { result ->
        result?.party?.pairs?.map { it.spinsSinceLastPaired }
            .assertIsEqualTo(
                listOf(0, 1, 3, null, null, null),
            )
    }
}
