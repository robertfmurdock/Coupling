package com.zegreatrob.coupling.dynamo.pin

import com.soywiz.klock.*
import com.zegreatrob.coupling.dynamo.DynamoPinRepository
import com.zegreatrob.coupling.dynamo.RepositoryContext
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.repository.validation.TribeContextData
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

@Suppress("unused")
class DynamoPinRepositoryTest : PinRepositoryValidator<DynamoPinRepository> {

    override val repositorySetup = asyncTestTemplate<TribeContext<DynamoPinRepository>>(sharedSetup = {
        val clock = MagicClock()
        val user = stubUser()
        TribeContextData(DynamoPinRepository(user.email, clock), stubTribeId(), clock, user)
    })

    @Test
    fun getPinRecordsWillShowAllRecordsIncludingDeletions() = asyncSetup(buildRepository { context ->
        object : Context by context {
            val tribeId = stubTribeId()
            val pin = stubPin()
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedPin = pin.copy(name = "CLONE")
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val updatedSaveTime2 = initialSaveTime.plus(4.hours)
        }
    }, additionalActions = {
        clock.currentTime = initialSaveTime
        repository.save(tribeId.with(pin))
        clock.currentTime = updatedSaveTime
        repository.save(tribeId.with(updatedPin))
        clock.currentTime = updatedSaveTime2
        repository.deletePin(tribeId, pin.id!!)
    }) exercise {
        repository.getPinRecords(tribeId)
    } verify { result ->
        result
            .assertContains(Record(tribeId.with(pin), user.email, false, initialSaveTime))
            .assertContains(Record(tribeId.with(updatedPin), user.email, false, updatedSaveTime))
            .assertContains(Record(tribeId.with(updatedPin), user.email, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup(buildRepository { context ->
        object : Context by context {
            val tribeId = stubTribeId()
            val records = listOf(
                tribeRecord(tribeId, stubPin(), uuidString(), false, DateTime.now().minus(3.months)),
                tribeRecord(tribeId, stubPin(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }
    }) exercise {
        records.forEach { repository.saveRawRecord(it) }
        repository.getPinRecords(tribeId)
    } verify { loadedRecords ->
        records.forEach { loadedRecords.assertContains(it) }
    }
}

private typealias Context = RepositoryContext<DynamoPinRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoPinRepository(user.email, clock) }()
}
