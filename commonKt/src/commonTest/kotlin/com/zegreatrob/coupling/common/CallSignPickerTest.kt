package com.zegreatrob.coupling.common

import com.zegreatrob.coupling.common.entity.callsign.AvailableComponents
import com.zegreatrob.coupling.common.entity.callsign.CallSign
import com.zegreatrob.coupling.common.entity.callsign.CallSignPicker
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
    }) exercise {
        components.pick(email)
    } verify { result ->
        result.assertIsEqualTo(CallSign("Excellent", "Tacos"))
    }

    @Test
    fun givenMultipleOptionsTheSameEmailWillAlwaysReturnTheSameCallSign() = setup(object {
        val adjectives = listOf("Red", "Green", "Blue")
        val nouns = listOf("Lion", "Tiger", "Bear")
        val components = AvailableComponents(adjectives, nouns)
        val email = "robert.f.murdock@accenture.com"
    }) exercise {
        components.pick(email) to components.pick(email)
    } verify { (result1, result2) ->
        result1.assertIsEqualTo(result2)
    }

    @Test
    fun givenTheSameSetOfOptionsDifferentEmailsWillProduceDifferentResults() = setup(object {
            val adjectives = listOf("Red", "Green", "Blue")
            val nouns = listOf("Lion", "Tiger", "Bear")
            val components = AvailableComponents(adjectives, nouns)
            val email1 = "robert.f.murdock@accenture.com"
            val email2 = "rmurdock@pillartechnology.com"
    }) exercise {
        components.pick(email1) to components.pick(email2)
    } verify { (result1, result2) ->
        result1.assertIsNotEqualTo(result2)
    }

}