package com.zegreatrob.coupling.common

import com.zegreatrob.coupling.common.entity.callsign.CallSign
import com.zegreatrob.coupling.common.entity.callsign.PickCallSignActionDispatcher
import com.zegreatrob.coupling.common.entity.callsign.GenerateCallSignAction
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PickCallSignActionTest {

    companion object : PickCallSignActionDispatcher;

    @Test
    fun whenGivenOnlyOneAdjectiveAndOneNounWillSelectThem() = setup(object {
        val adjective = "Excellent"
        val noun = "Tacos"
        val email = "robert.f.murdock@accenture.com"
        val action = GenerateCallSignAction(listOf(adjective), listOf(noun), email)
    }) exercise {
        action.pick()
    } verify { result ->
        result.assertIsEqualTo(CallSign("Excellent", "Tacos"))
    }

    @Test
    fun givenMultipleOptionsTheSameEmailWillAlwaysReturnTheSameCallSign() = setup(object {
        val adjectives = listOf("Red", "Green", "Blue")
        val nouns = listOf("Lion", "Tiger", "Bear")
        val email = "robert.f.murdock@accenture.com"
        val action = GenerateCallSignAction(adjectives, nouns, email)
    }) exercise {
        action.pick() to action.pick()
    } verify { (result1, result2) ->
        result1.assertIsEqualTo(result2)
    }

    @Test
    fun givenTheSameSetOfOptionsDifferentEmailsWillProduceDifferentResults() = setup(object {
        val adjectives = listOf("Red", "Green", "Blue")
        val nouns = listOf("Lion", "Tiger", "Bear")
        val action1 = GenerateCallSignAction(adjectives, nouns, "robert.f.murdock@accenture.com")
        val action2 = GenerateCallSignAction(adjectives, nouns, "rmurdock@pillartechnology.com")
    }) exercise {
        action1.pick() to action2.pick()
    } verify { (result1, result2) ->
        result1.assertIsNotEqualTo(result2)
    }

}