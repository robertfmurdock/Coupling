package com.zegreatrob.coupling.repository.dynamo.pin

import com.soywiz.klock.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.repository.dynamo.DynamoPinRepository
import com.zegreatrob.coupling.repository.dynamo.RepositoryContext
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@Suppress("unused")
@ExperimentalTime
class DynamoPinRepositoryTest : PinRepositoryValidator<DynamoPinRepository> {

    override val repositorySetup = asyncTestTemplate<PartyContext<DynamoPinRepository>>(sharedSetup = {
        val clock = MagicClock()
        val user = stubUser()
        PartyContextData(DynamoPinRepository(user.email, clock), stubPartyId(), clock, user)
    })

    @Test
    fun getPinRecordsWillShowAllRecordsIncludingDeletions() = asyncSetup.with(buildRepository { context ->
        object : Context by context {
            val tribeId = stubPartyId()
            val pin = stubPin()
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedPin = pin.copy(name = "CLONE")
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val updatedSaveTime2 = initialSaveTime.plus(4.hours)
        }
    }) exercise {
        clock.currentTime = initialSaveTime
        repository.save(tribeId.with(pin))
        clock.currentTime = updatedSaveTime
        repository.save(tribeId.with(updatedPin))
        clock.currentTime = updatedSaveTime2
        repository.deletePin(tribeId, pin.id!!)
    } verifyWithWait {
        repository.getPinRecords(tribeId)
            .assertContains(Record(tribeId.with(pin), user.email, false, initialSaveTime))
            .assertContains(Record(tribeId.with(updatedPin), user.email, false, updatedSaveTime))
            .assertContains(Record(tribeId.with(updatedPin), user.email, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup.with(buildRepository { context ->
        object : Context by context {
            val tribeId = stubPartyId()
            val records = listOf(
                partyRecord(tribeId, stubPin(), uuidString(), false, DateTime.now().minus(3.months)),
                partyRecord(tribeId, stubPin(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }
    }) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verifyWithWait {
        val loadedRecords = repository.getPinRecords(tribeId)
        records.forEach { loadedRecords.assertContains(it) }
    }
}

private typealias Context = RepositoryContext<DynamoPinRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoPinRepository(user.email, clock) }()
}
