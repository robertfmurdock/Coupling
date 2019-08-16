import com.soywiz.klock.DateTime
import com.soywiz.klock.internal.toDateTime
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.player.with
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.server.MonkToolkit
import com.zegreatrob.coupling.server.entity.player.MongoPlayerRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.js.*
import kotlin.random.Random
import kotlin.test.Test

private const val mongoUrl = "localhost/PlayersRepositoryTest"

class MongoPlayerRepositoryTest {

    companion object : MongoPlayerRepository, MonkToolkit {
        override val jsRepository: dynamic = jsRepository(mongoUrl)
        override val userEmail: String = "user-${Random.nextInt(200)}"

        val playerCollection: dynamic by lazy<dynamic> { getCollection("players", mongoUrl) }

        suspend fun dropPlayers() {
            playerCollection.drop().unsafeCast<Promise<Unit>>().await()
        }

        suspend fun getDbPlayers(tribeId: TribeId) =
                playerCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
    }

    @Test
    fun canSaveAndGetPlayer() = testAsync {
        dropPlayers()
        setupAsync(object {
            val tribeId = TribeId("woo")
            val player = Player(
                    id = id(),
                    badge = 1,
                    name = "Tim",
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "tim@tim.meat",
                    imageURL = "italian.jpg"
            )
        }) exerciseAsync {
            save(TribeIdPlayer(tribeId, player))
            getPlayersAsync(tribeId).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(player))
        }
    }

    @Test
    fun savedPlayersIncludeModificationDateAndUsername() = testAsync {
        dropPlayers()
        setupAsync(object {
            val tribeId = TribeId("woo")
            val player = Player(
                    id = id(),
                    badge = 1,
                    name = "Tim",
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "tim@tim.meat",
                    imageURL = "italian.jpg"
            )
        }) exerciseAsync {
            save(TribeIdPlayer(tribeId, player))
            getDbPlayers(tribeId)
        } verifyAsync { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                get("timestamp").unsafeCast<Date>().toDateTime()
                        .isCloseToNow()
                        .assertIsEqualTo(true)
                get("modifiedByUsername").assertIsEqualTo(userEmail)
            }
        }
    }

    class SavingTheSamePlayerTwice {

        private suspend fun setupSavedPlayer() = setupAsync(object {
            val tribeId = TribeId("boo")
            val player = Player(
                    id = id(),
                    badge = 1,
                    name = "Tim",
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "tim@tim.meat",
                    imageURL = "italian.jpg"
            )
            val updatedPlayer = player.copy(name = "Timmy")
        }) {
            dropPlayers()
            save(TribeIdPlayer(tribeId, player))
        }

        @Test
        fun willNotDeleteOriginalRecord() = testAsync {
            setupSavedPlayer() exerciseAsync {
                save(TribeIdPlayer(tribeId, updatedPlayer))
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
                save(TribeIdPlayer(tribeId, updatedPlayer))
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
            val tribe = TribeId("hoo")
            val playerId = id()
            val player = Player(
                    id = playerId,
                    badge = 0,
                    name = "Jim",
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "jim@jim.meat",
                    imageURL = "italian.jpg"
            )
        }) exerciseAsync {
            save(TribeIdPlayer(tribe, player))
            delete(playerId)
        } verifyAsync {
            getPlayersAsync(tribe).await().assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun whenPlayerIsDeletedWillShowUpInGetDeleted() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("hoo")
            val playerId = id()
            val player = Player(
                    id = playerId,
                    badge = 0,
                    name = "Jim",
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "jim@jim.meat",
                    imageURL = "italian.jpg"
            )
        }) {
            dropPlayers()
            save(TribeIdPlayer(tribeId, player))
            delete(playerId)
        } exerciseAsync {
            getDeletedAsync(tribeId).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(player))
        }
    }

    @Test
    fun whenPlayerIsDeletedThenBroughtBackThenDeletedWillShowUpOnceInGetDeleted() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("hoo")
            val playerId = id()
            val player = Player(
                    id = playerId,
                    badge = 0,
                    name = "Jim",
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "jim@jim.meat",
                    imageURL = "italian.jpg"
            )
        }) {
            dropPlayers()
            save(player with tribeId)
            delete(playerId)
            save(player with tribeId)
            delete(playerId)
        } exerciseAsync {
            getDeletedAsync(tribeId).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(player))
        }
    }

    @Test
    fun deleteRecordWillIncludeUsername() = testAsync {
        dropPlayers()
        setupAsync(object {
            val tribeId = TribeId("hoo")
            val playerId = id()
            val player = Player(
                    id = playerId,
                    badge = 0,
                    name = "Jim",
                    callSignAdjective = "Spicy",
                    callSignNoun = "Meatball",
                    email = "jim@jim.meat",
                    imageURL = "italian.jpg"
            )
            val userWhoSaved = "user that saved"
        }) {
            with(object : MongoPlayerRepository {
                override val userEmail = userWhoSaved
                override val jsRepository = MongoPlayerRepositoryTest.jsRepository
            }) {
                save(TribeIdPlayer(tribeId, player))
            }
        } exerciseAsync {
            delete(playerId)
        } verifyAsync {
            getDbPlayers(tribeId)
                    .toList()
                    .sortedByDescending { it["timestamp"].unsafeCast<Date>().toDateTime() }
                    .map { it["modifiedByUsername"].unsafeCast<String>() }
                    .assertIsEqualTo(listOf(
                            userEmail,
                            userWhoSaved
                    ))
        }
    }

    class ForLegacyData {

        companion object {
            private suspend fun setupLegacyPlayer() = setupAsync(object {
                val playerId = id()
                val tribeId = TribeId("woo")
                val playerDbJson = json(
                        "_id" to playerId,
                        "tribe" to tribeId.value,
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
                        id = playerId,
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
                val tribeId = TribeId("woo")
                val playerDbJson = json(
                        "_id" to playerId,
                        "tribe" to tribeId.value,
                        "name" to "The Foul Monster"
                )

                val updatedPlayer = Player(
                        id = playerId,
                        name = "Clean Monster"
                )
            }) {
                dropPlayers()
                playerCollection.insert(playerDbJson).unsafeCast<Promise<Unit>>().await()
            } exerciseAsync {
                save(TribeIdPlayer(tribeId, updatedPlayer))
                getPlayersAsync(tribeId).await()
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(updatedPlayer))
            }
        }
    }

    @Test
    fun forRealisticLegacyData() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("nah")
            val playerDbJson = json(
                    "_id" to "5c59ca700e6e5e3cce737c6e",
                    "tribe" to tribeId.value,
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
            val tribeIdPlayer = Player(
                    id = "5c59ca700e6e5e3cce737c6e",
                    name = "Guy guy",
                    email = "duder",
                    badge = 2
            )
            save(TribeIdPlayer(tribeId, tribeIdPlayer))

            playerCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
        } exerciseAsync {
            getPlayersAsync(tribeId).await()
        } verifyAsync { result ->
            result[0].badge.assertIsEqualTo(2)
        }
    }

    @Test
    fun deleteWithUnknownPlayerIdWillReturnFalse() = testAsync {
        setupAsync(object {
            val playerId = id()
        }) exerciseAsync {
            delete(playerId)
        } verifyAsync { result -> result.assertIsEqualTo(false) }
    }

    @Test
    fun getPlayersForEmailsWillReturnLatestVersionOfPlayers() = testAsync {
        setupAsync(object {
            val email = "test@zegreatrob.com"
            val player = Player(id(), email = email, name = "Testo")
            val redHerring = Player(id(), email = "somethingelse", name = "Testo")
            val updatedPlayer = player.copy(name = "Besto")
            val tribeId = TribeId("test")
        }) {
            dropPlayers()
            save(player with tribeId)
            save(redHerring with tribeId)
            save(updatedPlayer with tribeId)
        } exerciseAsync {
            getPlayersByEmailAsync(email).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(updatedPlayer with tribeId))
        }
    }

    @Test
    fun getPlayersForEmailsWillNotIncludePlayersThatChangedTheirEmailToSomethingElse() = testAsync {
        setupAsync(object {
            val email = "test@zegreatrob.com"
            val player = Player(id(), email = email, name = "Testo")
            val updatedPlayer = player.copy(name = "Besto", email = "something else ")
            val tribeId = TribeId("test")
        }) {
            dropPlayers()
            save(player with tribeId)
            save(updatedPlayer with tribeId)
        } exerciseAsync {
            getPlayersByEmailAsync(email).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

}