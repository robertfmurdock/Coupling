package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.player.callsign.GenerateCallSignAction
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class GenerateCallSignActionTest {

    companion object : GenerateCallSignAction.Dispatcher

    @Test
    fun whenGivenOnlyOneAdjectiveAndOneNounWillSelectThem() = setup(object {
        val adjective = "Excellent"
        val noun = "Tacos"
        val email = "robert.f.murdock@accenture.com"
        val action = GenerateCallSignAction(setOf(adjective), setOf(noun), email, emptyList())
    }) exercise {
        perform(action)
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
        perform(action) to perform(action)
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
        perform(action1) to perform(action2)
    } verify { (result1, result2) ->
        result1.assertIsNotEqualTo(result2)
    }

    @Test
    fun willUseDifferentAdjectiveIfAnotherPlayerAlreadyHasUsedIt() = setup(object {
        val adjectives = setOf("Red", "Green", "Blue")
        val nouns = setOf("Lion", "Tiger", "Bear")
        val email = "robert.f.murdock@accenture.com"
        val normalGeneratedCallSign = CallSign("Green", "Bear")
        val players = listOf(stubPlayer().copy(callSignAdjective = normalGeneratedCallSign.adjective))

        val action = GenerateCallSignAction(adjectives, nouns, email, players)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(CallSign("Blue", "Bear"))
    }

    @Test
    fun willUseDifferentNounIfAnotherPlayerAlreadyHasUsedIt() = setup(object {
        val adjectives = setOf("Red", "Green", "Blue")
        val nouns = setOf("Lion", "Tiger", "Bear")
        val email = "robert.f.murdock@accenture.com"
        val normalGeneratedCallSign = CallSign("Green", "Bear")
        val players = listOf(stubPlayer().copy(callSignNoun = normalGeneratedCallSign.noun))

        val action = GenerateCallSignAction(adjectives, nouns, email, players)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(CallSign("Green", "Tiger"))
    }

    @Test
    fun willReturnBlanksWhenAllTermsAreUsed() = setup(object {
        val adjectives = setOf("Red", "Green", "Blue")
        val nouns = setOf("Lion", "Tiger", "Bear")
        val email = "robert.f.murdock@accenture.com"
        val players = listOf(
            stubPlayer().copy(callSignAdjective = "Red", callSignNoun = "Lion"),
            stubPlayer().copy(callSignAdjective = "Green", callSignNoun = "Tiger"),
            stubPlayer().copy(callSignAdjective = "Blue", callSignNoun = "Bear"),
        )

        val action = GenerateCallSignAction(adjectives, nouns, email, players)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(CallSign("Blank", "Blank"))
    }
}
