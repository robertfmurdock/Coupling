package com.zegreatrob.coupling.common

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.callsign.CallSign
import com.zegreatrob.coupling.common.entity.player.callsign.GenerateCallSignActionDispatcher
import com.zegreatrob.coupling.common.entity.player.callsign.GenerateCallSignAction
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class GenerateCallSignActionTest {

    companion object : GenerateCallSignActionDispatcher;

    @Test
    fun whenGivenOnlyOneAdjectiveAndOneNounWillSelectThem() = setup(object {
        val adjective = "Excellent"
        val noun = "Tacos"
        val email = "robert.f.murdock@accenture.com"
        val action = GenerateCallSignAction(setOf(adjective), setOf(noun), email, emptyList())
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(CallSign("Excellent", "Tacos"))
    }

    @Test
    fun givenMultipleOptionsTheSameEmailWillAlwaysReturnTheSameCallSign() = setup(object {
        val adjectives = setOf("Red", "Green", "Blue")
        val nouns = setOf("Lion", "Tiger", "Bear")
        val email = "robert.f.murdock@accenture.com"
        val action = GenerateCallSignAction(adjectives, nouns, email, emptyList())
    }) exercise {
        action.perform() to action.perform()
    } verify { (result1, result2) ->
        result1.assertIsEqualTo(result2)
    }

    @Test
    fun givenTheSameSetOfOptionsDifferentEmailsWillProduceDifferentResults() = setup(object {
        val adjectives = setOf("Red", "Green", "Blue")
        val nouns = setOf("Lion", "Tiger", "Bear")
        val action1 = GenerateCallSignAction(adjectives, nouns, "robert.f.murdock@accenture.com", emptyList())
        val action2 = GenerateCallSignAction(adjectives, nouns, "rmurdock@pillartechnology.com", emptyList())
    }) exercise {
        action1.perform() to action2.perform()
    } verify { (result1, result2) ->
        result1.assertIsNotEqualTo(result2)
    }

    @Test
    fun willUseDifferentAdjectiveIfAnotherPlayerAlreadyHasUsedIt() = setup(object {
        val adjectives = setOf("Red", "Green", "Blue")
        val nouns = setOf("Lion", "Tiger", "Bear")
        val email = "robert.f.murdock@accenture.com"
        val normalGeneratedCallSign = CallSign("Green", "Bear")
        val players = listOf(Player(callSignAdjective = normalGeneratedCallSign.adjective))

        val action = GenerateCallSignAction(adjectives, nouns, email, players)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(CallSign("Blue", "Bear"))
    }

    @Test
    fun willUseDifferentNounIfAnotherPlayerAlreadyHasUsedIt() = setup(object {
        val adjectives = setOf("Red", "Green", "Blue")
        val nouns = setOf("Lion", "Tiger", "Bear")
        val email = "robert.f.murdock@accenture.com"
        val normalGeneratedCallSign = CallSign("Green", "Bear")
        val players = listOf(Player(callSignNoun = normalGeneratedCallSign.noun))

        val action = GenerateCallSignAction(adjectives, nouns, email, players)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(CallSign("Green", "Tiger"))
    }

}