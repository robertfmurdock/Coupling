import kotlin.test.Test
import kotlin.test.assertEquals

class SpinCommandTest {

    class WithNoPlayers : SpinCommandDispatcher {
        override val actionDispatcher: GetNextPairActionDispatcher = StubGetNextPairActionDispatcher()
        override val wheel: Wheel = object : Wheel {}

        @Test
        fun withNoPlayersShouldReturnNoPairs() {
            val result = SpinCommand(Game(listOf(), listOf(), PairingRule.LongestTime))
                    .perform()
            assertEquals(result, listOf())
        }
    }

    class WithTwoPlayersTest : SpinCommandDispatcher {

        private val bill: Player = KtPlayer(_id = "Bill")
        private val ted: Player = KtPlayer(_id = "Ted")
        private val players = listOf(bill, ted)

        override val actionDispatcher = StubGetNextPairActionDispatcher()
        override val wheel = StubWheel()

        @Test
        fun playersShouldBeRemovedFromWheelBeforeEachPlay() {
            wheel.returnValues.add(bill)
            actionDispatcher.returnValues.add(PairCandidateReport(ted, listOf(bill), TimeResultValue(0)))

            SpinCommand(Game(listOf(), players, PairingRule.LongestTime))
                    .perform()
                    .assertIsEqualTo(listOf(CouplingPair.Double(ted, bill)))

            actionDispatcher.receivedValues.getOrNull(0)
                    .assertIsEqualTo(GetNextPairAction(GameSpin(listOf(), players, PairingRule.LongestTime)))

            wheel.receivedValues.assertContains(listOf(bill))
        }
    }

    class WithThreePlayersInSingledOutMode : SpinCommandDispatcher {

        private val bill: Player = KtPlayer(_id = "Bill")
        private val ted: Player = KtPlayer(_id = "Ted")
        private val mozart: Player = KtPlayer(_id = "Mozart")
        private val players = listOf(bill, ted, mozart)

        override val actionDispatcher = StubGetNextPairActionDispatcher()
        override val wheel = StubWheel()

        @Test
        fun shouldRemoveAPlayerFromTheWheelBeforeEachPlay() {
            actionDispatcher.willReturn(listOf(
                    PairCandidateReport(mozart, listOf(bill, ted), TimeResultValue(0)),
                    PairCandidateReport(ted, emptyList(), TimeResultValue(0))
            ))
            wheel.willReturn(bill)

            SpinCommand(Game(listOf(), players, PairingRule.LongestTime))
                    .perform()
                    .assertIsEqualTo(
                            listOf(CouplingPair.Double(mozart, bill), CouplingPair.Single(ted))
                    )

            actionDispatcher.receivedValues
                    .assertIsEqualTo(listOf(
                            GetNextPairAction(GameSpin(listOf(), players, PairingRule.LongestTime)),
                            GetNextPairAction(GameSpin(listOf(), listOf(ted), PairingRule.LongestTime))
                    ))

            wheel.receivedValues
                    .assertContains(listOf(bill, ted))
        }
    }
}

class SpyImpl<I, O> : Spy<I, O> {
    override val receivedValues = mutableListOf<I>()
    override val returnValues = mutableListOf<O>()
}

interface Spy<I, O> {
    val receivedValues: MutableList<I>
    val returnValues: MutableList<O>
    fun spyFunction(input: I) = returnValues.popValue()!!.also { receivedValues.add(input) }

    fun willReturn(values: Collection<O>) {
        returnValues += values
    }

    fun willReturn(value: O) {
        returnValues += value
    }
}

class StubWheel : Wheel, Spy<List<Player>, Player> by SpyImpl() {
    override fun Array<Player>.spin(): Player = spyFunction(this.toList())
}

class StubGetNextPairActionDispatcher : GetNextPairActionDispatcher,
        Spy<GetNextPairAction, PairCandidateReport> by SpyImpl() {
    override val actionDispatcher get() = throw NotImplementedError()
    override fun GetNextPairAction.perform() = spyFunction(this)
}

fun <T> MutableList<T>.popValue() = getOrNull(0)?.also { removeAt(0) }

fun <T> T?.assertIsEqualTo(expected: T, message: String? = null) = assertEquals(expected, this, message)

fun <T> MutableList<T>.assertContains(item: T) = contains(item)
        .assertIsEqualTo(true, "${this.map { "$item" }} did not contain $item")
        .let { this }