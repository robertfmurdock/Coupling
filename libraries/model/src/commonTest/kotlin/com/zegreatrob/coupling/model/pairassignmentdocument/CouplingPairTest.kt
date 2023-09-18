package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair.Companion.equivalent
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class CouplingPairTest {

    @Test
    fun pairsWithSwappedPositionShouldBeEquivalent() = setup(object {
        private val player1 = defaultPlayer.copy(id = "1")
        private val player2 = defaultPlayer.copy(id = "2")
        val pair1 = pairOf(player1, player2)
        val pair2 = pairOf(player2, player1)
    }) exercise {
        equivalent(pair1, pair2)
    } verify { result ->
        result.assertIsEqualTo(true)
    }

    @Test
    fun differentPairsAreNotEquivalent() = setup(object {
        private val player1 = defaultPlayer.copy(id = "1")
        private val player2 = defaultPlayer.copy(id = "2")
        private val player3 = defaultPlayer.copy(id = "3")
        val pair1 = pairOf(player1, player2)
        val pair2 = pairOf(player1, player3)
    }) exercise {
        equivalent(pair1, pair2)
    } verify { result ->
        result.assertIsEqualTo(false)
    }
}
