package com.zegreatrob.coupling.common

import com.zegreatrob.coupling.common.entity.callsign.AvailableComponents
import com.zegreatrob.coupling.common.entity.callsign.CallSign
import com.zegreatrob.coupling.common.entity.callsign.CallSignPicker
import com.zegreatrob.coupling.common.entity.callsign.PickCallSignAction
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class CallSignPickerTest {

    companion object : CallSignPicker;

    @Test
    fun whenGivenOnlyOneAdjectiveAndOneNounWillSelectThem() = setup(object {
        val adjective = "Excellent"
        val noun = "Tacos"
        val components = AvailableComponents(listOf(adjective), listOf(noun))
        val email = "robert.f.murdock@accenture.com"
        val action = PickCallSignAction(components, email)
    }) exercise {
        action.pick()
    } verify { result ->
        result.assertIsEqualTo(CallSign("Excellent", "Tacos"))
    }

    @Test
    fun givenMultipleOptionsTheSameEmailWillAlwaysReturnTheSameCallSign() = setup(object {
        val adjectives = listOf("Red", "Green", "Blue")
        val nouns = listOf("Lion", "Tiger", "Bear")
        val components = AvailableComponents(adjectives, nouns)
        val email = "robert.f.murdock@accenture.com"
        val action = PickCallSignAction(components, email)
    }) exercise {
        action.pick() to action.pick()
    } verify { (result1, result2) ->
        result1.assertIsEqualTo(result2)
    }

    @Test
    fun givenTheSameSetOfOptionsDifferentEmailsWillProduceDifferentResults() = setup(object {
            val adjectives = listOf("Red", "Green", "Blue")
            val nouns = listOf("Lion", "Tiger", "Bear")
            val components = AvailableComponents(adjectives, nouns)
        val action1 = PickCallSignAction(components, "robert.f.murdock@accenture.com")
        val action2 = PickCallSignAction(components, "rmurdock@pillartechnology.com")
    }) exercise {
        action1.pick() to action2.pick()
    } verify { (result1, result2) ->
        result1.assertIsNotEqualTo(result2)
    }

}