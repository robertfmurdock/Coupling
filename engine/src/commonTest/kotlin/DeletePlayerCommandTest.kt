import kotlin.test.Test

class DeletePlayerCommandTest {
    @Test
    fun willUseRepositoryToRemove() = testAsync {
        setupAsync(object : DeletePlayerCommandDispatcher {
            val playerId = "ThatGuyGetHim"
            override val playerRepository = PlayerRepositorySpy().apply { whenever(playerId, Unit) }
        }) exerciseAsync {
            DeletePlayerCommand(playerId)
                    .perform()
        } verifyAsync { result -> result.assertIsEqualTo(playerId) }
    }

    class PlayerRepositorySpy : PlayerRepository, Spy<String, Unit> by SpyData() {
        override suspend fun delete(playerId: String) = spyFunction(playerId)

        override fun getPlayersAsync(tribeId: TribeId) = cancel()
        override suspend fun save(tribeIdPlayer: TribeIdPlayer) = cancel()
    }
}