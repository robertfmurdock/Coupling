import kotlin.test.Test
import kotlin.test.assertEquals

class SpinCommandTest {

    class WithNoPlayers : SpinCommandDispatcher {
        override val actionDispatcher: GetNextPairActionDispatcher = object : GetNextPairActionDispatcher {
            override val actionDispatcher get() = throw NotImplementedError()

            override fun GetNextPairAction.perform(): PairCandidateReport? {
                return null
            }
        }
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

            actionDispatcher.allActionsPerformed.getOrNull(0)
                    .assertIsEqualTo(GetNextPairAction(GameSpin(listOf(), players, PairingRule.LongestTime)))

            wheel.allArraysSpun.assertContains(listOf(bill))
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
            actionDispatcher.returnValues
                    .plusAssign(listOf(
                            PairCandidateReport(mozart, listOf(bill, ted), TimeResultValue(0)),
                            PairCandidateReport(ted, emptyList(), TimeResultValue(0))
                    ))
            wheel.returnValues.add(bill)

            SpinCommand(Game(listOf(), players, PairingRule.LongestTime))
                    .perform()
                    .assertIsEqualTo(
                            listOf(CouplingPair.Double(mozart, bill), CouplingPair.Single(ted))
                    )

            actionDispatcher.allActionsPerformed
                    .assertIsEqualTo(listOf(
                            GetNextPairAction(GameSpin(listOf(), players, PairingRule.LongestTime)),
                            GetNextPairAction(GameSpin(listOf(), listOf(ted), PairingRule.LongestTime))
                    ))

            wheel.allArraysSpun
                    .assertContains(listOf(bill, ted))
        }
    }
}


class StubWheel : Wheel {
    val allArraysSpun = mutableListOf<List<Player>>()
    val returnValues = mutableListOf<Player>()
    override fun Array<Player>.spin(): Player = returnValues.popValue()!!.also { allArraysSpun.add(this.asList()) }
}

class StubGetNextPairActionDispatcher : GetNextPairActionDispatcher {
    val allActionsPerformed = mutableListOf<GetNextPairAction>()
    val returnValues = mutableListOf<PairCandidateReport>()
    override val actionDispatcher get() = throw NotImplementedError()

    override fun GetNextPairAction.perform() = returnValues.popValue()
            .also { allActionsPerformed.add(this) }
}

fun <T> MutableList<T>.popValue() = getOrNull(0)?.also { removeAt(0) }

fun <T> T?.assertIsEqualTo(expected: T, message: String? = null) = assertEquals(expected, this, message)

fun <T> MutableList<T>.assertContains(item: T) = contains(item)
        .assertIsEqualTo(true, "${this.map { "$item" }} did not contain $item")
        .let { this }