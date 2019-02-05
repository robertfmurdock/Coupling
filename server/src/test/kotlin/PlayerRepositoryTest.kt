
import com.soywiz.klock.DateTime
import com.soywiz.klock.internal.toDateTime
import com.soywiz.klock.seconds
import kotlinx.coroutines.await
import kotlin.js.*
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

        suspend fun getDbPlayers(tribeId: String): Array<Json> {
            @Suppress("UNUSED_VARIABLE")
            val monk = js("require(\"monk\")")
            val db = js("monk.default('$mongoUrl')")
            return db.get("players").find(json("tribe" to tribeId)).unsafeCast<Promise<Array<Json>>>().await()
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
    fun savedPlayersIncludeModificationDate() = testAsync {
        dropPlayers()
        setupAsync(object {
            val tribeId = "woo"
            val player = Player(
                    _id = id(),
                    badge = 1,
                    tribe = tribeId,
                    name = "Tim",
                    pins = emptyList(),
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "tim@tim.meat",
                    imageURL = "italian.jpg"
            )
        }) exerciseAsync {
            save(player)
            getDbPlayers(tribeId)
        } verifyAsync { result ->
            result.size.assertIsEqualTo(1)
            result.firstOrNull()?.get("timestamp").unsafeCast<Date>().toDateTime()
                    .isCloseToNow()
                    .assertIsEqualTo(true)
        }
    }

    private fun DateTime.isCloseToNow() = (DateTime.now() - this) < 1.seconds

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