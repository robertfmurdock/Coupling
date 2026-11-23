package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.stubmodel.record
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PairingHeatmapCellsTest {
    @Test
    fun markerIncludesOnlyCellsWithinOnePairingOfTheBaseline() = setup(object {
        val players = List(4) { stubPlayer() }
        val partyId = stubPartyId()
        val pairIndexes = listOf(0 to 1, 0 to 2, 0 to 3, 1 to 2, 1 to 3, 2 to 3)
        val pairs = pairIndexes.mapIndexed { count, (first, second) ->
            PlayerPair(listOf(players[first], players[second]).map { record(partyId, it) }, recentTimesPaired = count, pairAssignmentHistory = emptyList())
        }
    }) exercise {
        pairingHeatmapCells(players, pairs, 3, 6)
    } verify { cells ->
        pairIndexes.map { (first, second) -> cells[first][second].outlined }
            .assertIsEqualTo(listOf(false, true, true, true, false, false))
        pairIndexes.map { (first, second) -> cells[second][first].outlined }
            .assertIsEqualTo(listOf(false, true, true, true, false, false))
        players.indices.map { cells[it][it].outlined }.assertIsEqualTo(listOf(false, false, false, false))
    }

    @Test
    fun missingPairCountsAreDescribedAsUnavailableAndNeverMarked() = setup(object {
        val players = listOf(stubPlayer().copy(name = "Alex"), stubPlayer().copy(name = "Blair"))
    }) exercise {
        pairingHeatmapCells(players, emptyList(), 1, 2)
    } verify { cells ->
        cells[0][1].description.assertIsEqualTo("Alex / Blair. Recent pairings: Unavailable. Expected: 2.0. Unavailable. Spins since last paired: Unavailable.")
        cells[0][1].outlined.assertIsEqualTo(false)
    }

    @Test
    fun unavailableBaselineDoesNotInventAMarkerForAKnownCount() = setup(object {
        val players = listOf(stubPlayer().copy(name = "Alex"), stubPlayer().copy(name = "Blair"))
        val partyId = stubPartyId()
        val pairs = listOf(PlayerPair(players.map { record(partyId, it) }, recentTimesPaired = 0, pairAssignmentHistory = emptyList()))
    }) exercise {
        pairingHeatmapCells(players, pairs, 1, null)
    } verify { cells ->
        cells[0][1].description.assertIsEqualTo("Alex / Blair. Recent pairings: 0. Expected: Unavailable. Unavailable. Spins since last paired: Never paired.")
        cells[0][1].outlined.assertIsEqualTo(false)
    }
}
