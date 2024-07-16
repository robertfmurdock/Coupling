package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.pairassignmentdocument.Wheel
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.random.Random
import kotlin.test.Test

class WheelTest : Wheel {

    var nextRandomIndex: Int = 0

    override val random: Random = object : Random() {
        override fun nextBits(bitCount: Int) = 0
        override fun nextInt(until: Int): Int = nextRandomIndex
    }

    @Test
    fun randomlyChoosesAPersonOnTheWheel() {
        arrayOf("Scooby", "Shaggy", "Scrappy")
            .map { defaultPlayer.copy(id = it) }
            .toTypedArray()
            .checkSpinWorksForIndex(1)
            .checkSpinWorksForIndex(0)
            .checkSpinWorksForIndex(2)
    }

    private fun Array<Player>.checkSpinWorksForIndex(expectedIndex: Int): Array<Player> = also {
        nextRandomIndex = expectedIndex
        val actual = spin()
        val expected = this[expectedIndex]
        actual.assertIsEqualTo(expected)
    }
}
