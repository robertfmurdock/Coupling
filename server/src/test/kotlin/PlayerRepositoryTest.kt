import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.test.Test

const val mongoUrl = "localhost/PlayersRepositoryTest"

fun jsRepository(): dynamic {
    @Suppress("UNUSED_VARIABLE")
    val clazz = js("require('../../../../lib/CouplingDataService').default")
    return js("new clazz('$mongoUrl')")
}

class PlayerRepositoryTest {

    companion object : PlayersRepository by MongoPlayerRepository(jsRepository()) {

        fun id(): String {
            @Suppress("UNUSED_VARIABLE")
            val monk = js("require(\"monk\")")
            return js("monk.id()").toString()
        }

        suspend fun dropPlayers() {
            @Suppress("UNUSED_VARIABLE")
            val monk = js("require(\"monk\")")
            val db = js("monk.default('$mongoUrl')")
            db.get("players").drop().unsafeCast<Promise<Unit>>().await()
        }
    }

    @Test
    fun canSaveAndGetPlayer() = testAsync {
        dropPlayers()
        setupAsync(object {
            val tribe = "woo"
            val player = Player(
                    _id = id(),
                    badge = 1,
                    tribe = tribe,
                    name = "Tim",
                    pins = emptyList(),
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "tim@tim.meat",
                    imageURL = "italian.jpg"
            )
        }) exerciseAsync {
            save(player)
            getPlayersAsync(tribe).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(player))
        }
    }

    @Test
    fun canSaveDeleteAndNotGetPlayer() = testAsync {
        dropPlayers()
        setupAsync(object {
            val tribe = "hoo"
            val playerId = id()
            val player = Player(
                    _id = playerId,
                    badge = 0,
                    tribe = tribe,
                    name = "Jim",
                    pins = emptyList(),
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "jim@jim.meat",
                    imageURL = "italian.jpg"
            )
        }) exerciseAsync {
            save(player)
            delete(playerId)
            getPlayersAsync(tribe).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

}