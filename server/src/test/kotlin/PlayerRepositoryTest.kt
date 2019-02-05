
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

        val playerCollection: dynamic by lazy<dynamic> {
            @Suppress("UNUSED_VARIABLE")
            val monk = js("require(\"monk\")")
            val db = js("monk.default('$mongoUrl')")
            db.get("players")
        }

        suspend fun dropPlayers() {
            playerCollection.drop().unsafeCast<Promise<Unit>>().await()
        }

        suspend fun getDbPlayers(tribeId: String) =
                playerCollection.find(json("tribe" to tribeId)).unsafeCast<Promise<Array<Json>>>().await()
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

    class SavingTheSamePlayerTwice {

        private suspend fun setupSavedPlayer() = setupAsync(object {
            val tribeId = "boo"
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
            val updatedPlayer = player.copy(name = "Timmy")
        }) {
            dropPlayers()
            save(player)
        }

        @Test
        fun willNotDeleteOriginalRecord() = testAsync {
            setupSavedPlayer() exerciseAsync {
                save(updatedPlayer)
                getDbPlayers(tribeId)
            } verifyAsync { result ->
                result.toList().sortedByDescending { it["timestamp"].unsafeCast<Date>().toDateTime() }
                        .map { it["name"] }
                        .assertIsEqualTo(listOf("Timmy", "Tim"))
            }
        }

        @Test
        fun getWillOnlyReturnTheUpdatedPlayer() = testAsync {
            setupSavedPlayer() exerciseAsync {
                save(updatedPlayer)
                getPlayersAsync(tribeId).await()
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(updatedPlayer))
            }
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

    class ForLegacyData {

        companion object {
            private suspend fun setupLegacyPlayer() = setupAsync(object {
                val playerId = id()
                val tribeId = "woo"
                val playerDbJson = json(
                        "_id" to playerId,
                        "tribe" to tribeId,
                        "name" to "The Foul Monster"
                )
            }) {
                dropPlayers()
                playerCollection.insert(playerDbJson).unsafeCast<Promise<Unit>>().await()
            }
        }

        @Test
        fun canGetPlayersThatLookHistorical() = testAsync {
            setupLegacyPlayer() exerciseAsync {
                getPlayersAsync(tribeId).await()
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(Player(
                        _id = playerId,
                        tribe = tribeId,
                        name = playerDbJson["name"].toString()
                )))
            }
        }

        @Test
        fun canDeletePlayersThatLookHistorical() = testAsync {
            setupLegacyPlayer() exerciseAsync {
                delete(playerId)
                getPlayersAsync(tribeId).await()
            } verifyAsync { result ->
                result.assertIsEqualTo(emptyList())
            }
        }

        @Test
        fun saveThenGetWillOnlyReturnTheUpdatedPlayer() = testAsync {
            setupAsync(object {
                val playerId = id()
                val tribeId = "woo"
                val playerDbJson = json(
                        "_id" to playerId,
                        "tribe" to tribeId,
                        "name" to "The Foul Monster"
                )

                val updatedPlayer = Player(
                        _id = playerId,
                        tribe = tribeId,
                        name = "Clean Monster"
                )
            }) {
                dropPlayers()
                playerCollection.insert(playerDbJson).unsafeCast<Promise<Unit>>().await()
            } exerciseAsync {
                save(updatedPlayer)
                getPlayersAsync(tribeId).await()
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(updatedPlayer))
            }
        }
    }

    @Test
    fun forRealisticLegacyData() = testAsync {
        setupAsync(object {
            val tribeId = "nah"
            val playerDbJson = json(
                    "_id" to "5c59ca700e6e5e3cce737c6e",
                    "tribe" to tribeId,
                    "name" to "Guy guy",
                    "email" to "duder",
                    "pins" to emptyArray<Json>(),
                    "badge" to 1,
                    "id" to null,
                    "timestamp" to Date(Date.parse("2019-02-05T17:40:00.058Z"))
            )
        }) {
            dropPlayers()
            playerCollection.insert(playerDbJson).unsafeCast<Promise<Unit>>().await()
            save(Player(
                    _id = "5c59ca700e6e5e3cce737c6e",
                    name = "Guy guy",
                    tribe = tribeId,
                    email = "duder",
                    badge = 2
            ))

            playerCollection.find(json("tribe" to tribeId)).unsafeCast<Promise<Array<Json>>>().await()
        } exerciseAsync {
            getPlayersAsync(tribeId).await()
        } verifyAsync { result ->
            result[0].badge.assertIsEqualTo(2)
        }
    }

    @Test
    fun deleteWithUnknownPlayerIdWillThrowException() = testAsync {
        setupAsync(object {
            val playerId = id()
        }) exerciseAsync {
            assertThrowsAsync {
                delete(playerId)
            }
        } verifyAsync { result ->
            result.message.assertIsEqualTo("Failed to remove the player because it did not exist.")
        }
    }

}