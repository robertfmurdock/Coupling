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
    fun missingPairCountsAreDescribedAsUnavailable() = setup(object {
        val players = listOf(stubPlayer().copy(name = "Alex"), stubPlayer().copy(name = "Blair"))
    }) exercise {
        pairingHeatmapCells(players, emptyList(), 1, 2)
    } verify { cells ->
        cells[0][1].description.assertIsEqualTo("Alex / Blair. Recent pairings: Unavailable. Even split: 2.0 per pair. Unavailable. Spins since last paired: Unavailable.")
    }

    @Test
    fun unavailableEvenSplitDoesNotReplaceAKnownCount() = setup(object {
        val players = listOf(stubPlayer().copy(name = "Alex"), stubPlayer().copy(name = "Blair"))
        val partyId = stubPartyId()
        val pairs = listOf(PlayerPair(players.map { record(partyId, it) }, recentTimesPaired = 0, pairAssignmentHistory = emptyList()))
    }) exercise {
        pairingHeatmapCells(players, pairs, 1, null)
    } verify { cells ->
        cells[0][1].description.assertIsEqualTo("Alex / Blair. Recent pairings: 0. Even split: Unavailable per pair. Unavailable. Spins since last paired: Never paired.")
    }
}
