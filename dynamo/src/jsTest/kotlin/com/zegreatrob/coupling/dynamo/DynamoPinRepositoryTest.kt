package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubPin
import stubTribeId
import stubUser
import uuidString
import kotlin.test.Test

@Suppress("unused")
class DynamoPinRepositoryTest : PinRepositoryValidator {
    override suspend fun withRepository(clock: TimeProvider, handler: suspend (PinRepository, TribeId, User) -> Unit) {
        val user = stubUser()
        handler(DynamoPinRepository(user.email, clock), stubTribeId(), user)
    }

    @Test
    fun getPinRecordsWillShowAllRecordsIncludingDeletions() = testAsync {
        val clock = MagicClock()
        val user = stubUser()
        val repository = DynamoPinRepository(user.email, clock)
        setupAsync(object {
            val tribeId = stubTribeId()
            val pin = stubPin()
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedPin = pin.copy(name = "CLONE")
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val updatedSaveTime2 = initialSaveTime.plus(4.hours)
        }) {
            println("setup")
            clock.currentTime = initialSaveTime
            repository.save(tribeId.with(pin))
            clock.currentTime = updatedSaveTime
            repository.save(tribeId.with(updatedPin))
            clock.currentTime = updatedSaveTime2
            repository.deletePin(tribeId, pin._id!!)
            println("save and deletes done")
        } exerciseAsync {
            repository.getPinRecords(tribeId)
        } verifyAsync { result ->
            result
                .also { println("resuslts ${JSON.stringify(it)}") }
                .assertContains(Record(tribeId.with(pin), user.email, false, initialSaveTime))
                .assertContains(Record(tribeId.with(updatedPin), user.email, false, updatedSaveTime))
                .assertContains(Record(tribeId.with(updatedPin), user.email, true, updatedSaveTime2))
        }
    }

    @Test
    fun canSaveRawRecord() = testAsync {
        val clock = MagicClock()
        val user = stubUser()
        val repository = DynamoPinRepository(user.email, clock)

        setupAsync(object {
            val tribeId = stubTribeId()
            val records = listOf(
                tribeRecord(tribeId, stubPin(), uuidString(), false, DateTime.now().minus(3.months)),
                tribeRecord(tribeId, stubPin(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }) exerciseAsync {
            records.forEach { repository.saveRawRecord(it) }
        } verifyAsync {
            with(repository.getPinRecords(tribeId)) {
                records.forEach { assertContains(it) }
            }
        }
    }

}
