package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.stats.heatmap.pairingBaseline
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PairingBaselineTest {
    @Test
    fun shortHistoryUsesFractionalRotations() = setup(object {
        val savedSpins = 2
        val rotationPeriod = 3
    }) exercise {
        pairingBaseline(savedSpins, rotationPeriod)
    } verify { baseline ->
        baseline?.savedSpinsInWindow.assertIsEqualTo(2)
        baseline?.expectedPairings.assertIsEqualTo(2.0 / 3)
    }

    @Test
    fun historyOlderThanFiveCyclesDoesNotRaiseTheBaseline() = setup(object {
        val savedSpins = 100
        val rotationPeriod = 7
    }) exercise {
        pairingBaseline(savedSpins, rotationPeriod)
    } verify { baseline ->
        baseline?.savedSpinsInWindow.assertIsEqualTo(35)
        baseline?.expectedPairings.assertIsEqualTo(5.0)
    }

    @Test
    fun noSavedSpinsHasZeroExpectedPairings() = setup(object {
        val savedSpins = 0
        val rotationPeriod = 3
    }) exercise {
        pairingBaseline(savedSpins, rotationPeriod)
    } verify { baseline ->
        baseline?.expectedPairings.assertIsEqualTo(0.0)
    }

    @Test
    fun unavailableHistoryDoesNotInventAnExpectation() = setup(object {
        val savedSpins: Int? = null
    }) exercise {
        pairingBaseline(savedSpins, 3)
    } verify { baseline ->
        baseline.assertIsEqualTo(null)
    }

    @Test
    fun invalidRotationPeriodDoesNotDivideByZero() = setup(object {
        val rotationPeriod = 0
    }) exercise {
        pairingBaseline(10, rotationPeriod)
    } verify { baseline ->
        baseline.assertIsEqualTo(null)
    }
}
