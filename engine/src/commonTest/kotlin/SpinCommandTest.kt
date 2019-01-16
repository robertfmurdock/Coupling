import kotlin.test.Test
import kotlin.test.assertEquals

class SpinCommandTest {

    @Test
    fun withNoPlayersShouldReturnNoPairs() = setup(object : SpinCommandDispatcher, Wheel {
        override val actionDispatcher = StubGetNextPairActionDispatcher()
        override val wheel = this
    }) exercise {
        SpinCommand(Game(listOf(), listOf(), PairingRule.LongestTime))
                .perform()
    } verify { assertEquals(it, listOf()) }


    @Test
    fun withTwoPlayersEachShouldBeRemovedFromWheelBeforeEachPlay() = setup(object : SpinCommandDispatcher {
        override val actionDispatcher = StubGetNextPairActionDispatcher()
        override val wheel = StubWheel()
        val bill: Player = KtPlayer(_id = "Bill")
        val ted: Player = KtPlayer(_id = "Ted")
        val players = listOf(bill, ted)

        init {
            wheel.spyReturnValues.add(bill)
            actionDispatcher.spyReturnValues.add(PairCandidateReport(ted, listOf(bill), TimeResultValue(0)))
        }
    }) exercise {
        SpinCommand(Game(listOf(), players, PairingRule.LongestTime))
                .perform()
    } verify { result ->
        result.assertIsEqualTo(listOf(CouplingPair.Double(ted, bill)))
        actionDispatcher.spyReceivedValues.getOrNull(0)
                .assertIsEqualTo(GetNextPairAction(GameSpin(listOf(), players, PairingRule.LongestTime)))
        wheel.spyReceivedValues.assertContains(listOf(bill))
    }

    @Test
    fun shouldRemoveAPlayerFromTheWheelBeforeEachPlay() = setup(object : SpinCommandDispatcher {
        override val actionDispatcher = StubGetNextPairActionDispatcher()
        override val wheel = StubWheel()
        val bill: Player = KtPlayer(_id = "Bill")
        val ted: Player = KtPlayer(_id = "Ted")
        val mozart: Player = KtPlayer(_id = "Mozart")
        val players = listOf(bill, ted, mozart)

        val pairCandidateReports = listOf(
                PairCandidateReport(mozart, listOf(bill, ted), TimeResultValue(0)),
                PairCandidateReport(ted, emptyList(), TimeResultValue(0))
        )

        init {
            actionDispatcher spyWillReturn pairCandidateReports
            wheel spyWillReturn bill
        }
    }) exercise {
        SpinCommand(Game(listOf(), players, PairingRule.LongestTime))
                .perform()
    } verify { result ->
        result.assertIsEqualTo(
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

class StubWheel : Wheel, Spy<List<Player>, Player> by SpyData() {
    override fun Array<Player>.spin(): Player = spyFunction(this.toList())
}

class StubGetNextPairActionDispatcher : GetNextPairActionDispatcher,
        Spy<GetNextPairAction, PairCandidateReport> by SpyData() {
    override val actionDispatcher get() = throw NotImplementedError()
    override fun GetNextPairAction.perform() = spyFunction(this)
}

fun <T> MutableList<T>.popValue() = getOrNull(0)?.also { removeAt(0) }