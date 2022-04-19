package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class FindCallSignActionTest {

    companion object : FindCallSignActionDispatcher {
        const val email = "robert.f.murdock@accenture.com"
        val expectedCallSign = CallSign(adjective = "Swift", noun = "Wildebeest")
    }

    @Test
    fun withSimpleSetupWillReturnCallSign() = setup(object {
        val players = listOf(
            Player(callSignAdjective = "Modest", callSignNoun = "Tiger"),
            Player(callSignAdjective = "Intense", callSignNoun = "Mongoose")
        )
    }) exercise {
        perform(FindCallSignAction(players, email))
    } verify { result ->
        result.assertIsEqualTo(expectedCallSign)
    }

    @Test
    fun givenNoCollisionTheNumberOfUsedCallSignsWillNotAffectTheGeneratedResult() = setup(object {
        val players = listOf(
            Player(callSignAdjective = "Intense", callSignNoun = "Mongoose")
        )
    }) exercise {
        perform(FindCallSignAction(players, email))
    } verify { result ->
        result.assertIsEqualTo(expectedCallSign)
    }

    @Test
    fun whenTheAdjectiveIsAlreadyUsedAnotherOneWillBeGenerated() = setup(object {
        val players = listOf(
            Player(
                callSignAdjective = expectedCallSign.adjective,
                callSignNoun = "Mongoose"
            )
        )
    }) exercise {
        perform(FindCallSignAction(players, email))
    } verify { result ->
        result.assertIsEqualTo(
            CallSign(
                adjective = "Secure",
                noun = expectedCallSign.noun
            )
        )
    }

    @Test
    fun whenTheNounIsAlreadyUsedAnotherOneWillBeGenerated() = setup(object {
        val players = listOf(
            Player(
                callSignAdjective = "Intense",
                callSignNoun = expectedCallSign.noun
            )
        )
    }) exercise {
        perform(FindCallSignAction(players, email))
    } verify { result ->
        result.assertIsEqualTo(
            CallSign(
                adjective = expectedCallSign.adjective,
                noun = "Lion"
            )
        )
    }
}
