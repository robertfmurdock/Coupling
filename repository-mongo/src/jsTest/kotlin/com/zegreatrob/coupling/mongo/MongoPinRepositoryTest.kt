package com.zegreatrob.coupling.mongo

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.test.Test

private const val mongoUrl = "localhost/PinsRepositoryTest"

private typealias MongoPinContextMint = TribeContextMint<MongoPinRepositoryTest.Companion.MongoPinRepositoryTestAnchor>

@Suppress("unused")
class MongoPinRepositoryTest : PinRepositoryValidator<MongoPinRepositoryTest.Companion.MongoPinRepositoryTestAnchor> {

    override val repositorySetup = asyncTestTemplate<TribeContext<MongoPinRepositoryTestAnchor>> { test ->
        val clock = MagicClock()
        val user = stubUser()
        val repositoryWithDb = MongoPinRepositoryTestAnchor(clock, user.email)
        try {
            test(TribeContextData(repositoryWithDb, stubTribeId(), clock, user))
        } finally {
            repositoryWithDb.close()
        }
    }

    @Test
    fun saveThenDeleteWith12CharacterStringPinIdWillWorkCorrectly() = repositorySetup(object : MongoPinContextMint() {
        val pin = stubPin().copy(id = "19377906-pin")
    }.bind()) {
        repository.save(tribeId.with(pin))
    } exercise {
        repository.deletePin(tribeId, pin.id!!)
        repository.getPins(tribeId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun getRecordsWillReturnAllRevisionsIncludingDeleted() = repositorySetup(object : MongoPinContextMint() {
        val pin = stubPin()
        val updatedPin = pin.copy(name = "CLONE")
        val initialTimestamp = DateTime.now().minus(3.days)
        val updatedTimestamp = initialTimestamp.plus(2.hours)
    }.bind()) {
        clock.currentTime = initialTimestamp
        repository.save(tribeId.with(pin))
        repository.deletePin(tribeId, pin.id!!)
        clock.currentTime = updatedTimestamp
        repository.save(tribeId.with(updatedPin))
    } exercise {
        repository.getPinRecords(tribeId)
    } verify { result ->
        result.assertContains(tribeRecord(tribeId, pin, user.email, false, initialTimestamp))
            .assertContains(tribeRecord(tribeId, pin, user.email, true, initialTimestamp))
            .assertContains(tribeRecord(tribeId, updatedPin, user.email, false, updatedTimestamp))
    }

    companion object {

        class MongoPinRepositoryTestAnchor(override val clock: TimeProvider, override val userId: String) :
            MongoPinRepository, MonkToolkit {
            private val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)

            suspend fun dropPins() = pinCollection.drop().unsafeCast<Promise<Unit>>().await()

            fun close() = db.close()
        }

    }

}