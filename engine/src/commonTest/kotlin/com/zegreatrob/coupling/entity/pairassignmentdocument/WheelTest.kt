import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.entity.pairassignmentdocument.Wheel
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
                .map { Player(id = it) }
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

