import kotlinx.coroutines.*
import kotlin.js.Date
import kotlin.random.Random
import kotlin.test.Test

class ProposeNewPairsCommandTest {

    @Test
    fun willUseRepositoryToGetThings() = GlobalScope.promise {
        setup(object : ProposeNewPairsCommandDispatcher, CouplingDataRepository {
            val players = listOf(Player(name = "John"))
            val pins = listOf(Pin(name = "Bobby"))
            val history = listOf(PairAssignmentDocument(Date(), emptyList(), ""))
            val tribe = KtTribe("Tribe Id! ${Random.nextInt(300)}", PairingRule.PreferDifferentBadge)

            override fun getPins(tribeId: String) = CompletableDeferred(pins)
                    .also { tribeId.assertIsEqualTo(tribe.id) }

            override fun getHistory(tribeId: String) = CompletableDeferred(history)
                    .also { tribeId.assertIsEqualTo(tribe.id) }

            override fun getTribe(tribeId: String): Deferred<KtTribe> = CompletableDeferred(tribe)
                    .also { tribeId.assertIsEqualTo(tribe.id) }

            override val repository: CouplingDataRepository = this
            override val actionDispatcher = SpyRunGameActionDispatcher()

            val expectedPairAssignmentDocument = PairAssignmentDocument(Date(), listOf(), tribe.id)

            init {
                actionDispatcher.spyReturnValues.add(expectedPairAssignmentDocument)
            }
        }) exercise {
            async {
                ProposeNewPairsCommand(tribe.id, players)
                        .perform()
            }
        } verify { resultDeferred ->
            async {
                val result = resultDeferred.await()
                result.assertIsEqualTo(expectedPairAssignmentDocument)
                actionDispatcher.spyReceivedValues.assertIsEqualTo(listOf(RunGameAction(players, pins, history, tribe)))
            }
        }
    }
}

class SpyRunGameActionDispatcher : RunGameActionDispatcher, Spy<RunGameAction, PairAssignmentDocument> by SpyData() {
    override val actionDispatcher: FindNewPairsActionDispatcher get() = cancel()

    override fun RunGameAction.perform(): PairAssignmentDocument = spyFunction(this)
}