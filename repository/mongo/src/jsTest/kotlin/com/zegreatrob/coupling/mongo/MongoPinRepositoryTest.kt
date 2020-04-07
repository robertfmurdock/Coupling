package com.zegreatrob.coupling.mongo

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import kotlin.js.Promise
import kotlin.test.Test

private const val mongoUrl = "localhost/PinsRepositoryTest"

@Suppress("unused")
class MongoPinRepositoryTest : PinRepositoryValidator {

    override suspend fun withRepository(
        clock: TimeProvider,
        handler: suspend (PinRepository, TribeId, User) -> Unit
    ) {
        val user = stubUser()
        withMongoRepository(clock, user) { handler(it, stubTribeId(), user) }
    }

    @Test
    fun saveThenDeleteWith12CharacterStringPinIdWillWorkCorrectly() = testRepository { repository, tribeId, _, _ ->
        setupAsync(object {
            val pin = stubPin().copy(_id = "19377906-pin")
        }) {
            repository.save(tribeId.with(pin))
        } exerciseAsync {
            repository.deletePin(tribeId, pin._id!!)
            repository.getPins(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun getRecordsWillReturnAllRevisionsIncludingDeleted() = testAsync {
        val clock = MagicClock()
        val user = stubUser()
        withMongoRepository(user = user, clock = clock) { repository ->
            setupAsync(object {
                val tribeId = stubTribeId()
                val pin = stubPin()
                val updatedPin = pin.copy(name = "CLONE")
                val initialTimestamp = DateTime.now().minus(3.days)
                val updatedTimestamp = initialTimestamp.plus(2.hours)
            }) {
                clock.currentTime = initialTimestamp
                repository.save(tribeId.with(pin))
                repository.deletePin(tribeId, pin._id!!)
                clock.currentTime = updatedTimestamp
                repository.save(tribeId.with(updatedPin))
            } exerciseAsync {
                repository.getPinRecords(tribeId)
            } verifyAsync { result ->
                result.assertContains(tribeRecord(tribeId, pin, user.email, false, initialTimestamp))
                    .assertContains(tribeRecord(tribeId, pin, user.email, true, initialTimestamp))
                    .assertContains(tribeRecord(tribeId, updatedPin, user.email, false, updatedTimestamp))
            }
        }
    }

    companion object {
        private fun repositoryWithDb(
            clock: TimeProvider,
            user: User
        ) = MongoPinRepositoryTestAnchor(clock, user.email)

        class MongoPinRepositoryTestAnchor(override val clock: TimeProvider, override val userId: String) :
            MongoPinRepository, MonkToolkit {
            private val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)

            suspend fun dropPins() = pinCollection.drop().unsafeCast<Promise<Unit>>().await()

            fun close() = db.close()
        }

        private suspend fun withMongoRepository(
            clock: TimeProvider,
            user: User,
            block: suspend (MongoPinRepositoryTestAnchor) -> Unit
        ) {
            val repositoryWithDb = repositoryWithDb(clock, user)
            try {
                block(repositoryWithDb)
            } finally {
                repositoryWithDb.close()
            }
        }
    }

}