package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.entity.player.callsign.CallSignOptions
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignOptionsAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignOptionsActionDispatcher
import com.zegreatrob.coupling.action.entity.player.callsign.defaultCallSignOptions
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class FindCallSignOptionsActionTest {

    companion object : FindCallSignOptionsActionDispatcher

    @Test
    fun willIncludeAllPredefinedAdjectivesAndNounsWhenNoPlayersHaveCallSigns() = setup(object {
        val players = listOf(
            Player(),
            Player()
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
            Player(callSignAdjective = "Modest", callSignNoun = "Tiger"),
            Player(callSignAdjective = "Intense", callSignNoun = "Mongoose")
        )
        val action = FindCallSignOptionsAction(players)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(
            CallSignOptions(
                adjectives = defaultCallSignOptions.adjectives - setOf("Modest", "Intense"),
                nouns = defaultCallSignOptions.nouns - setOf("Tiger", "Mongoose")
            )
        )
    }
}