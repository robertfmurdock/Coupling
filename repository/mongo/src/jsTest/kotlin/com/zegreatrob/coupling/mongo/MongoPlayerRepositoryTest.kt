package com.zegreatrob.coupling.mongo

import com.soywiz.klock.*
import com.soywiz.klock.js.toDateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.mongo.player.MongoPlayerRepository
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.await
import kotlin.js.*
import kotlin.js.Date
import kotlin.test.Test

private const val mongoUrl = "localhost/PlayersRepositoryTest"

private typealias MongoPlayerContextMint =
        TribeContextMint<MongoPlayerRepositoryTest.Companion.MongoPlayerRepositoryTestAnchor>

class MongoPlayerRepositoryTest :
    PlayerEmailRepositoryValidator<MongoPlayerRepositoryTest.Companion.MongoPlayerRepositoryTestAnchor> {

    override val repositorySetup = Companion.repositorySetup

    companion object :
        RepositoryValidator<Companion.MongoPlayerRepositoryTestAnchor, TribeContext<Companion.MongoPlayerRepositoryTestAnchor>> {

        override val repositorySetup = asyncTestTemplate<TribeContext<MongoPlayerRepositoryTestAnchor>> { test ->
            val user = stubUser()
            val clock = MagicClock()
            val repositoryWithDb = MongoPlayerRepositoryTestAnchor(user.email, clock)
            try {
                test(TribeContextData(repositoryWithDb, stubTribeId(), clock, user))
            } finally {
                repositoryWithDb.db.close()
            }
        }

        class MongoPlayerRepositoryTestAnchor(override val userId: String, override val clock: TimeProvider) :
            MongoPlayerRepository, MonkToolkit {
            val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)

            suspend fun dropPlayers() {
                playersCollection.drop().unsafeCast<Promise<Unit>>().await()
            }

            suspend fun getDbPlayers(tribeId: TribeId) =
                playersCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
        }

    }

    @Test
    fun getPlayerRecordsWillShowAllRecordsIncludingDeletions() = repositorySetup(object : MongoPlayerContextMint() {
        val player = stubPlayer()
        val initialSaveTime = DateTime.now().minus(3.days)
        val updatedPlayer = player.copy(name = "CLONE")
        val updatedSaveTime = initialSaveTime.plus(2.hours)
    }.bind()) {
        clock.currentTime = initialSaveTime
        repository.save(tribeId.with(player))
        clock.currentTime = updatedSaveTime
        repository.save(tribeId.with(updatedPlayer))
        repository.deletePlayer(tribeId, player.id!!)
    } exercise {
        repository.getPlayerRecords(tribeId)
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                Record(
                    data = tribeId.with(player),
                    modifyingUserId = user.email,
                    isDeleted = false,
                    timestamp = initialSaveTime
                ),
                Record(
                    data = tribeId.with(updatedPlayer),
                    modifyingUserId = user.email,
                    isDeleted = false,
                    timestamp = updatedSaveTime
                ),
                Record(
                    data = tribeId.with(updatedPlayer),
                    modifyingUserId = user.email,
                    isDeleted = true,
                    timestamp = updatedSaveTime
                )
            )
        )
    }

    @Test
    fun savedPlayersIncludeModificationDateAndUsernameInMongo() = repositorySetup(object : MongoPlayerContextMint() {
        val player = stubPlayer()
    }.bind()) exercise {
        repository.save(tribeId.with(player))
        repository.getDbPlayers(tribeId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            get("timestamp").unsafeCast<Date>().toDateTime()
                .isCloseToNow()
                .assertIsEqualTo(true)
            get("modifiedByUsername").assertIsEqualTo(user.email)
        }
    }

    @Test
    fun savingTwiceWillNotDeleteOriginalRecord() = repositorySetup(object : MongoPlayerContextMint() {
        val player = stubPlayer()
        val updatedPlayer = player.copy(name = "Timmy")
    }.bind()) {
        repository.save(tribeId.with(player))
    } exercise {
        repository.save(tribeId.with(updatedPlayer))
        repository.getDbPlayers(tribeId)
    } verify { result ->
        result.toList().sortedByDescending { it["timestamp"].unsafeCast<Date>().toDateTime() }
            .map { it["name"] }
            .assertIsEqualTo(listOf("Timmy", player.name))
    }

    private fun DateTime.isCloseToNow() = (DateTime.now() - this) < 1.seconds

    private fun mongoPlayerRepository(userId: String, jsRepository: dynamic) = object : MongoPlayerRepository {
        override val userId = userId
        override val clock: TimeProvider get() = TimeProvider
        override val jsRepository = jsRepository
    }

    @Test
    fun deleteRecordWillIncludeUsername() = repositorySetup({
        object : TribeContext<MongoPlayerRepositoryTestAnchor> by it {
            val playerId = repository.id()
            val player = stubPlayer().copy(id = playerId)
            val userWhoSaved = "user that saved"
        }
    }) {
        mongoPlayerRepository(userId = userWhoSaved, jsRepository = repository.jsRepository)
            .save(tribeId.with(player))
    } exercise {
        repository.deletePlayer(tribeId, playerId)
    } verify {
        repository.getDbPlayers(tribeId)
            .toList()
            .sortedByDescending { it["timestamp"].unsafeCast<Date>().toDateTime() }
            .map { it["modifiedByUsername"].unsafeCast<String>() }
            .assertIsEqualTo(
                listOf(
                    user.email,
                    userWhoSaved
                )
            )
    }

    class ForLegacyData {

        private val legacyPlayerSetup = repositorySetup.extend(sharedSetup = { parent ->
            object {
                val playerId = parent.repository.id()
                val tribeId = TribeId("woo")
                val playerDbJson = json(
                    "_id" to playerId,
                    "tribe" to tribeId.value,
                    "name" to "The Foul Monster"
                )
                val repository = parent.repository
            }.also {
                parent.repository.dropPlayers()
                parent.repository.playersCollection.insert(it.playerDbJson).unsafeCast<Promise<Unit>>().await()
            }
        })

        @Test
        fun canGetPlayersThatLookHistorical() = legacyPlayerSetup() exercise {
            repository.getPlayers(tribeId)
        } verify { result ->
            result.map { it.data.player }
                .assertIsEqualTo(
                    listOf(Player(id = playerId, name = playerDbJson["name"].toString()))
                )
        }

        @Test
        fun canDeletePlayersThatLookHistorical() = legacyPlayerSetup() exercise {
            repository.deletePlayer(tribeId, playerId)
            repository.getPlayers(tribeId)
        } verify { result ->
            result.assertIsEqualTo(emptyList())
        }

        @Test
        fun saveThenGetWillOnlyReturnTheUpdatedPlayer() = repositorySetup({
            object : TribeContext<MongoPlayerRepositoryTestAnchor> by it {
                val playerId = repository.id()
                val playerDbJson = json(
                    "_id" to playerId,
                    "tribe" to tribeId.value,
                    "name" to "The Foul Monster"
                )
                val updatedPlayer = Player(
                    id = playerId,
                    name = "Clean Monster"
                )
            }
        }) {
            repository.playersCollection.insert(playerDbJson).unsafeCast<Promise<Unit>>().await()
        } exercise {
            repository.save(tribeId.with(updatedPlayer))
            repository.getPlayers(tribeId)
        } verify { result ->
            result.map { it.data.player }
                .assertIsEqualTo(listOf(updatedPlayer))
        }

        @Test
        fun forRealisticLegacyData() = repositorySetup({
            object : TribeContext<MongoPlayerRepositoryTestAnchor> by it {
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
            }
        }) {
            repository.playersCollection.insert(playerDbJson).unsafeCast<Promise<Unit>>().await()
            val tribeIdPlayer = Player(
                id = "5c59ca700e6e5e3cce737c6e",
                name = "Guy guy",
                email = "duder",
                badge = 2
            )
            repository.save(tribeId.with(tribeIdPlayer))
            repository.playersCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
        } exercise {
            repository.getPlayers(tribeId)
        } verify { result ->
            result[0].data.player.badge.assertIsEqualTo(2)
        }
    }
}