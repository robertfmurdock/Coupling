package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair.Companion.equivalent
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class CouplingPairTest {

    @Test
    fun pairsWithSwappedPositionShouldBeEquivalent() = setup(object {
        private val player1 = Player(id = "1", avatarType = null)
        private val player2 = Player(id = "2", avatarType = null)
        val pair1 = pairOf(player1, player2)
        val pair2 = pairOf(player2, player1)
    }) exercise {
        equivalent(pair1, pair2)
    } verify { result ->
        result.assertIsEqualTo(true)
    }

    @Test
    fun differentPairsAreNotEquivalent() = setup(object {
        private val player1 = Player(id = "1", avatarType = null)
        private val player2 = Player(id = "2", avatarType = null)
        private val player3 = Player(id = "3", avatarType = null)
        val pair1 = pairOf(player1, player2)
        val pair2 = pairOf(player1, player3)
    }) exercise {
        equivalent(pair1, pair2)
    } verify { result ->
        result.assertIsEqualTo(false)
    }
}
