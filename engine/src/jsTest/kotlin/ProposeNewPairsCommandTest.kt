
import com.soywiz.klock.DateTime
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlin.random.Random
import kotlin.test.Test

class ProposeNewPairsCommandTest {

    @Test
    fun willUseRepositoryToGetThingsAsync() = testAsync {
        setupAsync(object : ProposeNewPairsCommandDispatcher, CouplingDataRepository {
            val players = listOf(Player(name = "John"))
            val pins = listOf(Pin(name = "Bobby"))
            val history = listOf(PairAssignmentDocument(DateTime.now(), emptyList(), ""))
            val tribe = KtTribe("Tribe Id! ${Random.nextInt(300)}", PairingRule.PreferDifferentBadge)
            override fun getPinsAsync(tribeId: String) = CompletableDeferred(pins)
                    .also { tribeId.assertIsEqualTo(tribe.id) }

            override fun getHistoryAsync(tribeId: String) = CompletableDeferred(history)
                    .also { tribeId.assertIsEqualTo(tribe.id) }

            override fun getTribeAsync(tribeId: String): Deferred<KtTribe> = CompletableDeferred(tribe)
                    .also { tribeId.assertIsEqualTo(tribe.id) }

            override fun getPlayersAsync(tribeId: String): Deferred<List<Player>> = CompletableDeferred(emptyList())

            override suspend fun save(player: Player, tribeId: String) = Unit
            override suspend fun delete(playerId: String)  = Unit

            override val repository: CouplingDataRepository = this
            override val actionDispatcher = SpyRunGameActionDispatcher()

            val expectedPairAssignmentDocument = PairAssignmentDocument(DateTime.now(), listOf(), tribe.id)

            init {
                actionDispatcher.spyReturnValues.add(expectedPairAssignmentDocument)
            }
        }) exerciseAsync {
            ProposeNewPairsCommand(tribe.id, players)
                    .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(expectedPairAssignmentDocument)
            actionDispatcher.spyReceivedValues.assertIsEqualTo(listOf(RunGameAction(players, pins, history, tribe)))
        }
    }
}

class SpyRunGameActionDispatcher : RunGameActionDispatcher, Spy<RunGameAction, PairAssignmentDocument> by SpyData() {
    override val actionDispatcher: FindNewPairsActionDispatcher get() = cancel()

    override fun RunGameAction.perform(): PairAssignmentDocument = spyFunction(this)
}