import kotlin.js.Date
import kotlin.test.Test

class RunGameCommandTest {

    @Test
    fun willBuildAGameRunWithAllAvailablePlayersAndThenReturnTheResults() = setup(object : RunGameActionDispatcher {
        override val actionDispatcher = SpySpinActionDispatcher()
        val expectedDate = Date()
        override fun currentDate() = expectedDate
        val tribe = KtTribe("1", PairingRule.LongestTime)
        val players = emptyList<Player>()
        val pins = emptyList<Pin>()
        val history = emptyList<HistoryDocument>()
        val expectedPairingAssignments = listOf(
                CouplingPair.Single(Player()),
                CouplingPair.Single(Player())
        )

        init {
            actionDispatcher.spyReturnValues.add(expectedPairingAssignments)
        }
    }) exercise {
        RunGameAction(players, pins, history, tribe).perform()
    } verify { result ->
        result.assertIsEqualTo(PairAssignmentDocument(expectedDate, expectedPairingAssignments, tribe.id))
    }
}

class SpySpinActionDispatcher : SpinActionDispatcher, Spy<SpinAction, List<CouplingPair>> by SpyData() {
    override val actionDispatcher: GetNextPairActionDispatcher get() = cancel()
    override val wheel: Wheel get() = cancel()
    override fun SpinAction.perform(): List<CouplingPair> = spyFunction(this)
}
