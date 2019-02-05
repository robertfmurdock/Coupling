import com.soywiz.klock.DateTime
import kotlin.test.Test

class RunGameActionTest {

    @Test
    fun willBuildAGameRunWithAllAvailablePlayersAndThenReturnTheResults() = setup(object : RunGameActionDispatcher {
        override val actionDispatcher = SpyFindNewPairsActionDispatcher()
        val expectedDate = DateTime.now()
        override fun currentDate() = expectedDate
        val tribe = KtTribe("1", PairingRule.LongestTime)
        val players = emptyList<Player>()
        val pins = emptyList<Pin>()
        val history = emptyList<PairAssignmentDocument>()
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
        result.assertIsEqualTo(PairAssignmentDocument(
                expectedDate,
                expectedPairingAssignments.map { PinnedCouplingPair(it.asArray().map { player -> player.withPins() }) },
                tribe.id
        ))
    }
}

class SpyFindNewPairsActionDispatcher : FindNewPairsActionDispatcher, Spy<FindNewPairsAction, List<CouplingPair>> by SpyData() {
    override val actionDispatcher: NextPlayerActionDispatcher get() = cancel()
    override val wheel: Wheel get() = cancel()
    override fun FindNewPairsAction.perform(): List<CouplingPair> = spyFunction(this)
}
