package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.player.callsign.CallSignOptions
import com.zegreatrob.coupling.action.player.callsign.FindCallSignOptionsAction
import com.zegreatrob.coupling.action.player.callsign.defaultCallSignOptions
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class FindCallSignOptionsActionTest {

    companion object : FindCallSignOptionsAction.Dispatcher

    @Test
    fun willIncludeAllPredefinedAdjectivesAndNounsWhenNoPlayersHaveCallSigns() = setup(object {
        val players = listOf(
            defaultPlayer.copy(),
            defaultPlayer.copy(),
        )
        val action = FindCallSignOptionsAction(players)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(defaultCallSignOptions)
    }

    @Test
    fun willExcludeAnyOptionsUsedByThePlayers() = setup(object {
        val players = listOf(
            defaultPlayer.copy(callSignAdjective = "Modest", callSignNoun = "Tiger"),
            defaultPlayer.copy(callSignAdjective = "Intense", callSignNoun = "Mongoose"),
        )
        val action = FindCallSignOptionsAction(players)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(
            CallSignOptions(
                adjectives = defaultCallSignOptions.adjectives - setOf("Modest", "Intense"),
                nouns = defaultCallSignOptions.nouns - setOf("Tiger", "Mongoose"),
            ),
        )
    }
}
