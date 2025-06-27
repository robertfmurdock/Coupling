package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.stats.PairReport
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.spinsSinceLastPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.toPairCombinations
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.time.Instant

class SortPairStatistics {
    fun makePlayers(numberOfPlayers: Int) = (1..numberOfPlayers)
        .map { number -> makePlayer("$number") }
    private fun makePlayer(id: String) = stubPlayer().copy(id = PlayerId(id.toNotBlankString().getOrThrow()))

    @Test
    fun withFourPlayersThePairReportsAreOrderedByLongestTimeSinceLastPairingAndPlayerIndex() = setup(object {
        val players = makePlayers(4)
        val player1 = players[0]
        val player2 = players[1]
        val player3 = players[2]
        val player4 = players[3]
        val inputPairReports = listOf(
            PairReport(pairOf(player1, player4), NeverPaired),
            PairReport(pairOf(player1, player2), TimeResultValue(1)),
            PairReport(pairOf(player1, player3), TimeResultValue(0)),
            PairReport(pairOf(player2, player3), NeverPaired),
            PairReport(pairOf(player2, player4), TimeResultValue(0)),
            PairReport(pairOf(player3, player4), TimeResultValue(1)),
        )
    }) exercise {
        inputPairReports
            .sortedByDescending(::timeSincePairSort)
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                PairReport(pairOf(player1, player4), NeverPaired),
                PairReport(pairOf(player2, player3), NeverPaired),
                PairReport(pairOf(player1, player2), TimeResultValue(1)),
                PairReport(pairOf(player3, player4), TimeResultValue(1)),
                PairReport(pairOf(player1, player3), TimeResultValue(0)),
                PairReport(pairOf(player2, player4), TimeResultValue(0)),
            ),
        )
    }

    @Test
    fun stillSortsCorrectlyWithLargeRealisticHistory() = setup(loadJsonPartySetup("realistic-sort-test-data/inputs.json")) {
    } exercise {
        val pairs = players.toPairCombinations()
        val historyRecords = history.map { Record(PartyElement(party.id, it), "test".toNotBlankString().getOrThrow(), false, Instant.DISTANT_PAST) }
        pairs.map { historyRecords.spinsSinceLastPair(it) }
            .sortedByDescending { it }
    } verify { result ->
        result.assertIsEqualTo(
            loadResource<Array<Int>>("realistic-sort-test-data/expectResults.json")
                .toList(),
        )
    }
}
