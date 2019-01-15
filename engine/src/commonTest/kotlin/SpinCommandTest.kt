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
            wheel.spyReturnValues.add(bill)
            actionDispatcher.spyReturnValues.add(PairCandidateReport(ted, listOf(bill), TimeResultValue(0)))

            SpinCommand(Game(listOf(), players, PairingRule.LongestTime))
                    .perform()
                    .assertIsEqualTo(listOf(CouplingPair.Double(ted, bill)))

            actionDispatcher.spyReceivedValues.getOrNull(0)
                    .assertIsEqualTo(GetNextPairAction(GameSpin(listOf(), players, PairingRule.LongestTime)))

            wheel.spyReceivedValues.assertContains(listOf(bill))
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
            actionDispatcher spyWillReturn listOf(
                    PairCandidateReport(mozart, listOf(bill, ted), TimeResultValue(0)),
                    PairCandidateReport(ted, emptyList(), TimeResultValue(0))
            )
            wheel spyWillReturn bill

            SpinCommand(Game(listOf(), players, PairingRule.LongestTime))
                    .perform()
                    .assertIsEqualTo(
                            listOf(CouplingPair.Double(mozart, bill), CouplingPair.Single(ted))
                    )

            actionDispatcher.spyReceivedValues
                    .assertIsEqualTo(listOf(
                            GetNextPairAction(GameSpin(listOf(), players, PairingRule.LongestTime)),
                            GetNextPairAction(GameSpin(listOf(), listOf(ted), PairingRule.LongestTime))
                    ))

            wheel.spyReceivedValues
                    .assertContains(listOf(bill, ted))
        }
    }
}

class StubWheel : Wheel, Spy<List<Player>, Player> by SpyData() {
    override fun Array<Player>.spin(): Player = spyFunction(this.toList())
}

class StubGetNextPairActionDispatcher : GetNextPairActionDispatcher,
        Spy<GetNextPairAction, PairCandidateReport> by SpyData() {
    override val actionDispatcher get() = throw NotImplementedError()
    override fun GetNextPairAction.perform() = spyFunction(this)
}

fun <T> MutableList<T>.popValue() = getOrNull(0)?.also { removeAt(0) }