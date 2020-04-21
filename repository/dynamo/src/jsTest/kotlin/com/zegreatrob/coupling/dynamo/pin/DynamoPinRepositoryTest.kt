package com.zegreatrob.coupling.dynamo.pin

import com.soywiz.klock.*
import com.zegreatrob.coupling.dynamo.DynamoPinRepository
import com.zegreatrob.coupling.dynamo.RepositoryContext
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync2
import kotlin.test.Test

@Suppress("unused")
class DynamoPinRepositoryTest : PinRepositoryValidator {
    override suspend fun withRepository(clock: TimeProvider, handler: suspend (PinRepository, TribeId, User) -> Unit) {
        val user = stubUser()
        handler(DynamoPinRepository(user.email, clock), stubTribeId(), user)
    }

    @Test
    fun getPinRecordsWillShowAllRecordsIncludingDeletions() = setupAsync2(contextProvider = buildRepository { context ->
        object : Context by context {
            val tribeId = stubTribeId()
            val pin = stubPin()
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedPin = pin.copy(name = "CLONE")
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val updatedSaveTime2 = initialSaveTime.plus(4.hours)
        }
    }) {
        clock.currentTime = initialSaveTime
        repository.save(tribeId.with(pin))
        clock.currentTime = updatedSaveTime
        repository.save(tribeId.with(updatedPin))
        clock.currentTime = updatedSaveTime2
        repository.deletePin(tribeId, pin._id!!)
    } exercise {
        repository.getPinRecords(tribeId)
    } verify { result ->
        result
            .assertContains(Record(tribeId.with(pin), user.email, false, initialSaveTime))
            .assertContains(Record(tribeId.with(updatedPin), user.email, false, updatedSaveTime))
            .assertContains(Record(tribeId.with(updatedPin), user.email, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = setupAsync2(buildRepository { context ->
        object : Context by context {
            val tribeId = stubTribeId()
            val records = listOf(
                tribeRecord(tribeId, stubPin(), uuidString(), false, DateTime.now().minus(3.months)),
                tribeRecord(tribeId, stubPin(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }
    }) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verify {
        with(repository.getPinRecords(tribeId)) {
            records.forEach { assertContains(it) }
        }
    }
}

private typealias Context = RepositoryContext<DynamoPinRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend () -> T =
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoPinRepository(user.email, clock) }
