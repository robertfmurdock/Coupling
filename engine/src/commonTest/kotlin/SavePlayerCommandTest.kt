import kotlin.test.Test

class SavePlayerCommandTest {

    @Test
    fun willSaveToRepository() = testAsync {
        setupAsync(object : SavePlayerCommandDispatcher {
            val player = Player(
                    id = "1",
                    badge = 1,
                    tribe = "woo",
                    name = "Tim",
                    pins = emptyList(),
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "tim@tim.meat",
                    imageURL = "italian.jpg"
            )
            override val repository = PlayersRepositorySpy().apply { whenever(player, Unit) }
        }) exerciseAsync {
            SavePlayerCommand(player)
                    .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(player)
        }
    }

    class PlayersRepositorySpy : PlayersRepository, Spy<Player, Unit> by SpyData() {
        override fun getPlayersAsync(tribeId: String) = cancel()

        override suspend fun save(player: Player) = spyFunction(player)
        override suspend fun delete(playerId: String)  = cancel()
    }
}