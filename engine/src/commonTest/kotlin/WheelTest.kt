import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class WheelTest : Wheel {

    var nextRandomIndex: Int = 0

    override val random: Random = object : Random() {
        override fun nextBits(bitCount: Int) = 0
        override fun nextInt(until: Int): Int = nextRandomIndex
    }

    @Test
    fun randomlyChoosesAPersonOnTheWheel() {
        arrayOf("Scooby", "Shaggy", "Scrappy")
                .checkSpinWorksForIndex(1)
                .checkSpinWorksForIndex(0)
                .checkSpinWorksForIndex(2)
    }

    private fun Array<String>.checkSpinWorksForIndex(expectedIndex: Int): Array<String> = also {
        nextRandomIndex = expectedIndex
        val actual = spin()
        val expected = this[expectedIndex]
        actual.isEqualTo(expected)
    }

    private fun String.isEqualTo(expected: String) {
        assertEquals(expected, this)
    }

}

