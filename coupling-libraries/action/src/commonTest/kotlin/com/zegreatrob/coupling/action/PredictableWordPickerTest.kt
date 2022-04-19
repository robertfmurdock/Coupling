package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.player.callsign.PredictableWordPicker
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

@Suppress("unused")
class PredictableWordPickerTest {

    class ChoosesTheNameUsingTheValueOfGivenString {

        companion object : PredictableWordPicker

        @Test
        fun consistently() = setup(object {
            val options = listOf("option1", "option2", "option3")
            val givenString = "emailjones@email.edu"
        }) exercise {
            Pair(
                options.pickForGiven(givenString),
                options.pickForGiven(givenString)
            )
        } verify { (result1, result2) ->
            result1.assertIsEqualTo(result2)
        }

        @Test
        fun withSimpleNumberInputStrings() = setup(object {
            val options = listOf("option1", "option2", "option3")
            val givenStrings = listOf("0", "1", "2")
        }) exercise {
            givenStrings.map {
                options.pickForGiven(it)
            }
        } verify { (result0, result1, result2) ->
            val (option0, option1, option2) = options
            result0.assertIsEqualTo(option0)
            result1.assertIsEqualTo(option1)
            result2.assertIsEqualTo(option2)
        }

        @Test
        fun evenIfValueIsGreaterThanNumberOfOptions() = setup(object {
            val options = listOf("option1", "option2", "option3")
            val givenStrings = listOf("1", "2", "3")
        }) exercise {
            givenStrings.map {
                options.pickForGiven(it)
            }
        } verify { (result0, result1, result2) ->
            val (option0, option1, option2) = options
            result0.assertIsEqualTo(option1)
            result1.assertIsEqualTo(option2)
            result2.assertIsEqualTo(option0)
        }

        @Test
        fun evenIfValueIsNonNumeric() = setup(object {
            val options = listOf(
                "option0",
                "option1",
                "option2",
                "option3",
                "option4",
                "option5",
                "option6",
                "option7",
                "option8",
                "option9"
            )
            val givenStrings = listOf("a", "b", "c")
        }) exercise {
            givenStrings.map {
                options.pickForGiven(it)
            }
        } verify { (result0, result1, result2) ->
            val (option7, option8, option9) = options.subList(7, 10)
            result0.assertIsEqualTo(option7)
            result1.assertIsEqualTo(option8)
            result2.assertIsEqualTo(option9)
        }

        @Test
        fun evenIfValueHasMoreThanOneNonNumericCharacter() = setup(object {
            val options = listOf("option0", "option1", "option2")
            val givenStrings = listOf("aa", "bbc", "Rob")
        }) exercise {
            givenStrings.map {
                options.pickForGiven(it)
            }
        } verify { (result0, result1, result2) ->
            val (option0, option1, option2) = options
            result0.assertIsEqualTo(option2)
            result1.assertIsEqualTo(option1)
            result2.assertIsEqualTo(option0)
        }
    }
}
