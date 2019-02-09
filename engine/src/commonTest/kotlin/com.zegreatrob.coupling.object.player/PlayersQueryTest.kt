import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlin.test.Test

class PlayersQueryTest {

    @Test
    fun willReturnPlayersFromRepository() = testAsync {
        setupAsync(object : PlayersQueryDispatcher {
            val tribeId = TribeId("Excellent Tribe")
            val players = listOf(
                    Player(id = "1"),
                    Player(id = "2"),
                    Player(id = "3")
            )
            override val playerRepository = PlayerRepositorySpy()
                    .apply { whenever(tribeId, CompletableDeferred(players)) }
        }) exerciseAsync {
            PlayersQuery(tribeId)
                    .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(players)
        }
    }

    class PlayerRepositorySpy : PlayerRepository, Spy<TribeId, Deferred<List<Player>>> by SpyData() {
        override fun getPlayersAsync(tribeId: TribeId) = spyFunction(tribeId)

        override suspend fun save(tribeIdPlayer: TribeIdPlayer) = cancel()
        override suspend fun delete(playerId: String) = cancel()
    }
}
