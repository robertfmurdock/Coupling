package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.stats.calculateFullRotation
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class CalculateFullRotationTest {

    companion object {
        fun makePlayers(numberOfPlayers: Int) = (1..numberOfPlayers)
            .map { number -> makePlayer("$number") }

        private fun makePlayer(id: String) = Player(id = id, avatarType = null)
    }

    @Test
    fun whenGivenOnePlayerWillReturnOne() = setup(object {
        val players = makePlayers(1)
    }) exercise {
        players.calculateFullRotation()
    } verify { spinsUntilFullRotation ->
        spinsUntilFullRotation.assertIsEqualTo(1)
    }

    @Test
    fun whenGivenTwoPlayersWillReturnOne() = setup(object {
        val players = makePlayers(2)
    }) exercise {
        players.calculateFullRotation()
    } verify { spinsUntilFullRotation ->
        spinsUntilFullRotation.assertIsEqualTo(1)
    }

    @Test
    fun whenGivenThreePlayersWillReturnThree() = setup(object {
        val players = makePlayers(3)
    }) exercise {
        players.calculateFullRotation()
    } verify { spinsUntilFullRotation ->
        spinsUntilFullRotation.assertIsEqualTo(3)
    }

    @Test
    fun whenGivenFourPlayersWillReturnThree() = setup(object {
        val players = makePlayers(4)
    }) exercise {
        players.calculateFullRotation()
    } verify { spinsUntilFullRotation ->
        spinsUntilFullRotation.assertIsEqualTo(3)
    }

    @Test
    fun whenGivenSevenPlayersWillReturnSeven() = setup(object {
        val players = makePlayers(7)
    }) exercise {
        players.calculateFullRotation()
    } verify { spinsUntilFullRotation ->
        spinsUntilFullRotation.assertIsEqualTo(7)
    }

    @Test
    fun whenGivenEightPlayersWillReturnSeven() = setup(object {
        val players = makePlayers(8)
    }) exercise {
        players.calculateFullRotation()
    } verify { spinsUntilFullRotation ->
        spinsUntilFullRotation.assertIsEqualTo(7)
    }
}
