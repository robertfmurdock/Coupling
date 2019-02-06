import kotlin.test.Test

class SavePlayerCommandTest {

    @Test
    fun willSaveToRepository() = testAsync {
        setupAsync(object : SavePlayerCommandDispatcher {
            val tribe = TribeId("woo")
            val player = Player(
                    id = "1",
                    badge = 1,
                    name = "Tim",
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "tim@tim.meat",
                    imageURL = "italian.jpg"
            )
            override val repository = PlayersRepositorySpy().apply { whenever(player, Unit) }
        }) exerciseAsync {
            SavePlayerCommand(player, tribe)
                    .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(player)
        }
    }

    class PlayersRepositorySpy : PlayersRepository, Spy<Player, Unit> by SpyData() {
        override fun getPlayersAsync(tribeId: TribeId) = cancel()

        override suspend fun save(player: Player, tribeId: TribeId) = spyFunction(player)
        override suspend fun delete(playerId: String) = cancel()
    }
}