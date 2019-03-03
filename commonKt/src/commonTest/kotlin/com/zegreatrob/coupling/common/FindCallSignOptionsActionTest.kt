package com.zegreatrob.coupling.common

import com.zegreatrob.coupling.common.entity.callsign.CallSignOptions
import com.zegreatrob.coupling.common.entity.callsign.FindCallSignOptionsAction
import com.zegreatrob.coupling.common.entity.callsign.FindCallSignOptionsActionDispatcher
import com.zegreatrob.coupling.common.entity.callsign.defaultOptions
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class FindCallSignOptionsActionTest {

    companion object : FindCallSignOptionsActionDispatcher

    @Test
    fun willIncludeAllPredefinedAdjectivesAndNounsWhenNoPlayersHaveCallSigns() = setup(object {
        val players = listOf(Player(), Player())
        val action = FindCallSignOptionsAction(players)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(defaultOptions)
    }

    @Test
    fun willExcludeAnyOptionsUsedByThePlayers() = setup(object {
        val players = listOf(
                Player(callSignAdjective = "Modest", callSignNoun = "Tiger"),
                Player(callSignAdjective = "Intense", callSignNoun = "Mongoose")
        )
        val action = FindCallSignOptionsAction(players)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(CallSignOptions(
                adjectives = defaultOptions.adjectives - setOf("Modest", "Intense"),
                nouns = defaultOptions.nouns - setOf("Tiger", "Mongoose")
        ))
    }
}