package com.zegreatrob.coupling.common

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.callsign.CallSign
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignCommand
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignCommandDispatcher
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class FindCallSignCommandTest {

    companion object : FindCallSignCommandDispatcher {
        val email = "robert.f.murdock@accenture.com"
        val expectedCallSign = CallSign(adjective = "Swift", noun = "Wildebeast")
    }

    @Test
    fun withSimpleSetupWillReturnCallSign() = setup(object {
        val players = listOf(
                Player(callSignAdjective = "Modest", callSignNoun = "Tiger"),
                Player(callSignAdjective = "Intense", callSignNoun = "Mongoose")
        )

        val command = FindCallSignCommand(players, email)
    }) exercise {
        command.perform()
    } verify { result ->
        result.assertIsEqualTo(expectedCallSign)
    }

    @Test
    fun givenNoCollisionTheNumberOfUsedCallSignsWillNotAffectTheGeneratedResult() = setup(object {
        val players = listOf(
                Player(callSignAdjective = "Intense", callSignNoun = "Mongoose")
        )
        val command = FindCallSignCommand(players, email)
    }) exercise {
        command.perform()
    } verify { result ->
        result.assertIsEqualTo(expectedCallSign)
    }

    @Test
    fun whenTheAdjectiveIsAlreadyUsedAnotherOneWillBeGenerated() = setup(object {
        val players = listOf(
                Player(callSignAdjective = expectedCallSign.adjective, callSignNoun = "Mongoose")
        )
        val command = FindCallSignCommand(players, email)
    }) exercise {
        command.perform()
    } verify { result ->
        result.assertIsEqualTo(CallSign(adjective = "Secure", noun = expectedCallSign.noun))
    }

    @Test
    fun whenTheNounIsAlreadyUsedAnotherOneWillBeGenerated() = setup(object {
        val players = listOf(
                Player(callSignAdjective = "Intense", callSignNoun = expectedCallSign.noun)
        )
        val command = FindCallSignCommand(players, email)
    }) exercise {
        command.perform()
    } verify { result ->
        result.assertIsEqualTo(CallSign(adjective = expectedCallSign.adjective, noun = "Lion"))
    }

}