import kotlin.js.Date
import kotlin.test.Test

class RunGameCommandTest {

    @Test
    fun willBuildAGameRunWithAllAvailablePlayersAndThenReturnTheResults() = setup(object : RunGameCommandDispatcher {
        override val actionDispatcher = SpySpinCommandDispatcher()
        val expectedDate = Date()
        override fun currentDate() = expectedDate
        val tribe = KtTribe("1", PairingRule.LongestTime)
        val players = emptyList<Player>()
        val pins = emptyList<Any>()
        val history = emptyList<HistoryDocument>()
        val expectedPairingAssignments = listOf(
                CouplingPair.Single(KtPlayer()),
                CouplingPair.Single(KtPlayer())
        )

        init {
            actionDispatcher.spyReturnValues.add(expectedPairingAssignments)
        }
    }) exercise {
        RunGameCommand(players, pins, history, tribe).perform()
    } verify { result ->
        result.assertIsEqualTo(PairAssignmentDocument(expectedDate, expectedPairingAssignments, tribe.id))
    }
}

class SpySpinCommandDispatcher : SpinCommandDispatcher, Spy<SpinCommand, List<CouplingPair>> by SpyData() {
    override val actionDispatcher: GetNextPairActionDispatcher get() = cancel()
    override val wheel: Wheel get() = cancel()
    override fun SpinCommand.perform(): List<CouplingPair> = spyFunction(this)
}
