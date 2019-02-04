import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlin.test.Test

class PlayersQueryTest {

    @Test
    fun willReturnPlayersFromRepository() = testAsync {
        setupAsync(object : PlayersQueryDispatcher {

            val tribeId = "Excellent Tribe"
            val players = listOf(
                    Player(_id = "1"),
                    Player(_id = "2"),
                    Player(_id = "3")
            )

            override val repository = SpyRepository()
                    .apply { whenever(tribeId, CompletableDeferred(players)) }

        }) exerciseAsync {
            PlayersQuery(tribeId)
                    .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(players)
        }
    }

}

class SpyRepository : CouplingDataRepository, Spy<String, Deferred<List<Player>>> by SpyData() {
    override fun getPlayersAsync(tribeId: String) = spyFunction((tribeId))

    override fun getPinsAsync(tribeId: String): Deferred<List<Pin>> = cancel()

    override fun getHistoryAsync(tribeId: String): Deferred<List<PairAssignmentDocument>> = cancel()

    override fun getTribeAsync(tribeId: String): Deferred<KtTribe> = cancel()
}
