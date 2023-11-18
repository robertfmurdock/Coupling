package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair.Companion.equivalent
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayer
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

    @Test
    fun willCreateSoloPair() = setup(object {
        val player = stubPlayer()
    }) exercise {
        listOf(player).toCouplingPair()
    } verify { result ->
        result.assertIsEqualTo(CouplingPair.Single(player))
    }

    @Test
    fun willCreateProperPair() = setup(object {
        val player1 = stubPlayer()
        val player2 = stubPlayer()
    }) exercise {
        listOf(player1, player2).toCouplingPair()
    } verify { result ->
        result.assertIsEqualTo(CouplingPair.Double(player1, player2))
    }

    @Test
    fun willCreateTripleAsMob() = setup(object {
        val player1 = stubPlayer()
        val player2 = stubPlayer()
        val player3 = stubPlayer()
    }) exercise {
        listOf(player1, player2, player3).toCouplingPair()
    } verify { result ->
        result.assertIsEqualTo(CouplingPair.Mob(player1, player2, player3, emptySet()))
    }

    @Test
    fun willCreateFiveAsMob() = setup(object {
        val player1 = stubPlayer()
        val player2 = stubPlayer()
        val player3 = stubPlayer()
        val player4 = stubPlayer()
        val player5 = stubPlayer()
    }) exercise {
        listOf(player1, player2, player3, player4, player5).toCouplingPair()
    } verify { result ->
        result.assertIsEqualTo(CouplingPair.Mob(player1, player2, player3, setOf(player4, player5)))
    }
}
