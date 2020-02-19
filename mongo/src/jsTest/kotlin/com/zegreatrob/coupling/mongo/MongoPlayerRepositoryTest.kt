package com.zegreatrob.coupling.mongo

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.js.toDateTime
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.player.with
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.player.MongoPlayerRepository
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.repository.validation.PlayerRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import stubPlayer
import stubTribeId
import stubUser
import kotlin.js.*
import kotlin.test.Test

private const val mongoUrl = "localhost/PlayersRepositoryTest"

class MongoPlayerRepositoryTest : PlayerRepositoryValidator {

    override suspend fun withRepository(handler: suspend (PlayerRepository, TribeId, User) -> Unit) {
        val user = stubUser()
        withMongoRepository(user) { handler(this, stubTribeId(), user) }
    }

    companion object {
        private fun repositoryWithDb(userEmail: String) = MongoPlayerRepositoryTestAnchor(userEmail, TimeProvider)

        class MongoPlayerRepositoryTestAnchor(override val userEmail: String, override val clock: TimeProvider) :
            MongoPlayerRepository, MonkToolkit {
            val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)

            suspend fun dropPlayers() {
                playersCollection.drop().unsafeCast<Promise<Unit>>().await()
            }

            suspend fun getDbPlayers(tribeId: TribeId) =
                playersCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
        }

        private inline fun withMongoRepository(
            user: User = stubUser(),
            block: MongoPlayerRepositoryTestAnchor.() -> Unit
        ) {
            val repositoryWithDb = repositoryWithDb(user.email)
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.db.close()
            }
        }
    }

    @Test
    fun savedPlayersIncludeModificationDateAndUsernameInMongo() = testAsync {
        withMongoRepository {
            dropPlayers()
            setupAsync(object {
                val tribeId = TribeId("woo")
                val player = stubPlayer()
            }) exerciseAsync {
                save(tribeId.with(player))
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
    }

    @Test
    fun savingTwiceWillNotDeleteOriginalRecord() = testAsync {
        withMongoRepository {
            setupAsync(object {
                val tribeId = TribeId("boo")
                val player = stubPlayer()
                val updatedPlayer = player.copy(name = "Timmy")
            }) {
                dropPlayers()
                save(tribeId.with(player))
            } exerciseAsync {
                save(tribeId.with(updatedPlayer))
                getDbPlayers(tribeId)
            } verifyAsync { result ->
                result.toList().sortedByDescending { it["timestamp"].unsafeCast<Date>().toDateTime() }
                    .map { it["name"] }
                    .assertIsEqualTo(listOf("Timmy", player.name))
            }
        }
    }

    private fun DateTime.isCloseToNow() = (DateTime.now() - this) < 1.seconds

    @Test
    fun deleteRecordWillIncludeUsername() = testAsync {
        withMongoRepository {
            dropPlayers()
            setupAsync(object {
                val tribeId = TribeId("hoo")
                val playerId = id()
                val player = stubPlayer().copy(id = playerId)
                val userWhoSaved = "user that saved"
            }) {
                with(object : MongoPlayerRepository {
                    override val userEmail = userWhoSaved
                    override val clock: TimeProvider get() = TimeProvider
                    override val jsRepository = this@withMongoRepository.jsRepository
                }) {
                    save(tribeId.with(player))
                }
            } exerciseAsync {
                deletePlayer(tribeId, playerId)
            } verifyAsync {
                getDbPlayers(tribeId)
                    .toList()
                    .sortedByDescending { it["timestamp"].unsafeCast<Date>().toDateTime() }
                    .map { it["modifiedByUsername"].unsafeCast<String>() }
                    .assertIsEqualTo(
                        listOf(
                            userEmail,
                            userWhoSaved
                        )
                    )
            }
        }
    }

    class ForLegacyData {

        companion object {
            private suspend fun MongoPlayerRepositoryTestAnchor.setupLegacyPlayer() = setupAsync(object {
                val playerId = id()
                val tribeId = TribeId("woo")
                val playerDbJson = json(
                    "_id" to playerId,
                    "tribe" to tribeId.value,
                    "name" to "The Foul Monster"
                )
            }) {
                dropPlayers()
                playersCollection.insert(playerDbJson).unsafeCast<Promise<Unit>>().await()
            }
        }

        @Test
        fun canGetPlayersThatLookHistorical() = testAsync {
            withMongoRepository {
                setupLegacyPlayer() exerciseAsync {
                    getPlayers(tribeId)
                } verifyAsync { result ->
                    result.map { it.data.player }
                        .assertIsEqualTo(
                            listOf(Player(id = playerId, name = playerDbJson["name"].toString()))
                        )
                }
            }
        }

        @Test
        fun canDeletePlayersThatLookHistorical() = testAsync {
            withMongoRepository {
                setupLegacyPlayer() exerciseAsync {
                    deletePlayer(tribeId, playerId)
                    getPlayers(tribeId)
                } verifyAsync { result ->
                    result.assertIsEqualTo(emptyList())
                }
            }
        }

        @Test
        fun saveThenGetWillOnlyReturnTheUpdatedPlayer() = testAsync {
            withMongoRepository {
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
                    playersCollection.insert(playerDbJson).unsafeCast<Promise<Unit>>().await()
                } exerciseAsync {
                    save(tribeId.with(updatedPlayer))
                    getPlayers(tribeId)
                } verifyAsync { result ->
                    result.map { it.data.player }
                        .assertIsEqualTo(listOf(updatedPlayer))
                }
            }
        }
    }

    @Test
    fun forRealisticLegacyData() = testAsync {
        withMongoRepository {
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
                playersCollection.insert(playerDbJson).unsafeCast<Promise<Unit>>().await()
                val tribeIdPlayer = Player(
                    id = "5c59ca700e6e5e3cce737c6e",
                    name = "Guy guy",
                    email = "duder",
                    badge = 2
                )
                save(tribeId.with(tribeIdPlayer))

                playersCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
            } exerciseAsync {
                getPlayers(tribeId)
            } verifyAsync { result ->
                result[0].data.player.badge.assertIsEqualTo(2)
            }
        }
    }


    @Test
    fun getPlayersForEmailsWillReturnLatestVersionOfPlayers() = testAsync {
        withMongoRepository {
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
                getPlayersByEmail(email)
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(updatedPlayer with tribeId))
            }
        }
    }

    @Test
    fun getPlayersForEmailsWillNotIncludePlayersThatChangedTheirEmailToSomethingElse() = testAsync {
        withMongoRepository {
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
                getPlayersByEmail(email)
            } verifyAsync { result ->
                result.assertIsEqualTo(emptyList())
            }
        }
    }
}