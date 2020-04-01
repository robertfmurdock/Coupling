package com.zegreatrob.coupling.mongo

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoTribeRepositoryTest"

class MongoTribeRepositoryTest : TribeRepositoryValidator {

    override suspend fun withRepository(clock: TimeProvider, handler: suspend (TribeRepository, User) -> Unit) {
        withMongoRepository(clock) { repository, user -> handler(repository, user) }
    }

    private suspend fun withMongoRepository(
        clock: TimeProvider,
        handler: suspend (MongoTribeRepository, User) -> Unit
    ) {
        val user = stubUser()
        withMongoRepository(user, clock) { handler(this, user) }
    }

    private fun testMongoRepository(block: suspend CoroutineScope.(MongoTribeRepository, User, MagicClock) -> Any?) =
        testAsync {
            val clock = MagicClock()
            withMongoRepository(clock) { repository, user -> block(repository, user, clock) }
        }

    @Test
    fun getTribeRecordListWillIncludeAllSavesOfTribeIncludingDelete() = testMongoRepository { repository, user, clock ->
        setupAsync(object {
            val tribe = stubTribe()
            val updatedTribe = tribe.copy(name = "CLONE")
            val firstSaveTime = DateTime.now().minus(3.days)
            val secondSaveTime = firstSaveTime.plus(3.days)
        }) {
            clock.currentTime = firstSaveTime
            repository.save(tribe)
            clock.currentTime = secondSaveTime
            repository.save(updatedTribe)
            repository.delete(tribe.id)
        } exerciseAsync {
            repository.getTribeRecordList()
        } verifyAsync { result ->
            result.filter { it.data.id == tribe.id }.let {
                it[0].apply {
                    modifyingUserEmail.assertIsEqualTo(user.email)
                    timestamp.assertIsEqualTo(firstSaveTime)
                    isDeleted.assertIsEqualTo(false)
                }
                it[1].apply {
                    modifyingUserEmail.assertIsEqualTo(user.email)
                    timestamp.assertIsEqualTo(secondSaveTime)
                    isDeleted.assertIsEqualTo(false)
                }
                it[2].apply {
                    modifyingUserEmail.assertIsEqualTo(user.email)
                    timestamp.assertIsEqualTo(secondSaveTime)
                    isDeleted.assertIsEqualTo(true)
                }
            }
        }
    }

    companion object {

        class MongoTribeRepositoryTestAnchor(
            override val userEmail: String,
            override val clock: TimeProvider
        ) : MongoTribeRepository, MonkToolkit {
            val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)
        }

        private fun repositoryWithDb(user: User, clock: TimeProvider) =
            MongoTribeRepositoryTestAnchor(user.email, clock)

        private inline fun withMongoRepository(
            user: User = stubUser(),
            clock: TimeProvider = TimeProvider,
            block: MongoTribeRepositoryTestAnchor.() -> Unit
        ) {
            val repositoryWithDb = repositoryWithDb(user, clock)
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.db.close()
            }
        }
    }

    @Test
    fun canLoadTribeFromOldSchema() = testAsync {
        withMongoRepository {
            setupAsync(object {
                val expectedTribe = Tribe(
                    id = TribeId("safety"),
                    pairingRule = PairingRule.LongestTime,
                    defaultBadgeName = "Default",
                    alternateBadgeName = "Alternate",
                    name = "Safety Dance"
                )
            }) {
                tribesCollection.insert(
                    json(
                        "pairingRule" to 1,
                        "defaultBadgeName" to "Default",
                        "alternateBadgeName" to "Alternate",
                        "name" to "Safety Dance",
                        "id" to "safety"
                    )
                ).unsafeCast<Promise<Unit>>().await()
                Unit
            } exerciseAsync {
                getTribeRecord(expectedTribe.id)
            } verifyAsync { result ->
                result?.data
                    .assertIsEqualTo(expectedTribe)
            }
        }
    }


}