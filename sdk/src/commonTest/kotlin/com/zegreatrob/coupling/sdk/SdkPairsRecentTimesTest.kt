package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.PartyPairsRecentTimesPairedQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.time.Clock

class SdkPairsRecentTimesTest {

    companion object Companion : AssignPinsAction.Dispatcher {
        private fun pairAssignmentDocument(player1: Player, player2: Player) = PairingSet(
            id = PairingSetId.new(),
            date = Clock.System.now(),
            pairs = notEmptyListOf(pairOf(player1, player2)).withPins(),
            null,
        )
    }

    @Test
    fun withNoPlayersReturnsNoData() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = emptyList<Player>()
        val history = emptyList<PairingSet>()
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(GqlQuery(PartyPairsRecentTimesPairedQuery(party.id)))
    } verify { result ->
        result?.party?.pairList?.map { it.recentTimesPaired }
            .assertIsEqualTo(emptyList())
    }

    @Test
    fun withOnePlayerShowsOnePairWithoutRecentTimes() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = listOf(stubPlayer())
        val history = emptyList<PairingSet>()
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(
            GqlQuery(PartyPairsRecentTimesPairedQuery(party.id)),
        )
    } verify { result ->
        result?.party?.pairList?.map { it.recentTimesPaired }
            .assertIsEqualTo(listOf(null))
    }

    @Test
    fun withThreePlayersAndNoHistoryProducesThreePairsWithZero() = asyncSetup(object {
        val players = listOf(
            stubPlayer(),
            stubPlayer(),
            stubPlayer(),
        )
        val history = emptyList<PairingSet>()
        val party = stubPartyDetails()
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(GqlQuery(PartyPairsRecentTimesPairedQuery(party.id)))
    } verify { result ->
        result?.party?.pairList?.map { it.recentTimesPaired }
            .assertIsEqualTo(listOf(0, 0, 0, null, null, null))
    }

    @Test
    fun withTwoPlayersAndShortHistoryProducesOneRecentPairing() = asyncSetup(object {
        val players = listOf(
            stubPlayer(),
            stubPlayer(),
        )
        val history = listOf(pairAssignmentDocument(players[0], players[1]))
        val party = stubPartyDetails()
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(GqlQuery(PartyPairsRecentTimesPairedQuery(party.id)))
    } verify { result ->
        result?.party?.pairList?.map { it.recentTimesPaired }
            .assertIsEqualTo(listOf(1, null, null))
    }

    @Test
    fun withTwoPlayersAndFullHistoryProducesHigherPairingNumber() = asyncSetup(object {
        val players = listOf(
            stubPlayer(),
            stubPlayer(),
        )
        val party = stubPartyDetails()
        val history = listOf(
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
        )
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(GqlQuery(PartyPairsRecentTimesPairedQuery(party.id)))
    } verify { result ->
        result?.party?.pairList?.map { it.recentTimesPaired }
            .assertIsEqualTo(listOf(5, null, null))
    }

    @Test
    fun withThreePlayersAndInterestingHistoryProducesAccurateNumbers() = asyncSetup(object {
        val players = listOf(
            stubPlayer().copy(name = "0"),
            stubPlayer().copy(name = "1"),
            stubPlayer().copy(name = "2"),
        )
        val party = stubPartyDetails()
        val history = listOf(
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[2]),
        )
    }) {
        with(sdk()) {
            fire(SavePartyCommand(party))
            players.forEach {
                fire(SavePlayerCommand(party.id, it))
            }
            history.forEach {
                fire(SavePairAssignmentsCommand(party.id, it))
            }
        }
    } exercise {
        sdk().fire(GqlQuery(PartyPairsRecentTimesPairedQuery(party.id)))
    } verify { result ->
        result?.party?.pairList?.map { it.recentTimesPaired }
            .assertIsEqualTo(listOf(14, 1, 0, null, null, null))
    }
}
