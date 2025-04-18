package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class FindCallSignActionTest {

    companion object : FindCallSignAction.Dispatcher {
        const val EMAIL = "robert.f.murdock@accenture.com"
        val expectedCallSign = CallSign(adjective = "Swift", noun = "Wildebeest")
    }

    @Test
    fun withSimpleSetupWillReturnCallSign() = setup(object {
        val players = listOf(
            stubPlayer().copy(callSignAdjective = "Modest", callSignNoun = "Tiger"),
            stubPlayer().copy(callSignAdjective = "Intense", callSignNoun = "Mongoose"),
        )
    }) exercise {
        perform(FindCallSignAction(players, EMAIL))
    } verify { result ->
        result.assertIsEqualTo(expectedCallSign)
    }

    @Test
    fun givenNoCollisionTheNumberOfUsedCallSignsWillNotAffectTheGeneratedResult() = setup(object {
        val players = listOf(
            stubPlayer().copy(callSignAdjective = "Intense", callSignNoun = "Mongoose"),
        )
    }) exercise {
        perform(FindCallSignAction(players, EMAIL))
    } verify { result ->
        result.assertIsEqualTo(expectedCallSign)
    }

    @Test
    fun whenTheAdjectiveIsAlreadyUsedAnotherOneWillBeGenerated() = setup(object {
        val players = listOf(
            stubPlayer().copy(
                callSignAdjective = expectedCallSign.adjective,
                callSignNoun = "Mongoose",
                avatarType = null,
            ),
        )
    }) exercise {
        perform(FindCallSignAction(players, EMAIL))
    } verify { result ->
        result.assertIsEqualTo(
            CallSign(
                adjective = "Secure",
                noun = expectedCallSign.noun,
            ),
        )
    }

    @Test
    fun whenTheNounIsAlreadyUsedAnotherOneWillBeGenerated() = setup(object {
        val players = listOf(
            stubPlayer().copy(
                callSignAdjective = "Intense",
                callSignNoun = expectedCallSign.noun,
            ),
        )
    }) exercise {
        perform(FindCallSignAction(players, EMAIL))
    } verify { result ->
        result.assertIsEqualTo(
            CallSign(
                adjective = expectedCallSign.adjective,
                noun = "Lion",
            ),
        )
    }
}
