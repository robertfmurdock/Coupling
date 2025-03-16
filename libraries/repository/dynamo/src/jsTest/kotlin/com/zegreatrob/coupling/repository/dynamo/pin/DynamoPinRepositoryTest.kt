package com.zegreatrob.coupling.repository.dynamo.pin

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.repository.dynamo.DynamoPinRepository
import com.zegreatrob.coupling.repository.dynamo.RepositoryContext
import com.zegreatrob.coupling.repository.dynamo.now
import com.zegreatrob.coupling.repository.dynamo.pairs.months
import com.zegreatrob.coupling.repository.dynamo.pairs.years
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PartyContextData
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Suppress("unused")
class DynamoPinRepositoryTest : PinRepositoryValidator<DynamoPinRepository> {

    override val repositorySetup = asyncTestTemplate<PartyContext<DynamoPinRepository>>(sharedSetup = {
        val clock = MagicClock()
        val user = stubUserDetails()
        PartyContextData(DynamoPinRepository(user.email, clock), stubPartyId(), clock, user)
    })

    @Test
    fun getPinRecordsWillShowAllRecordsIncludingDeletions() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context {
                val partyId = stubPartyId()
                val pin = stubPin()
                val initialSaveTime = now().minus(3.days)
                val updatedPin = pin.copy(name = "CLONE")
                val updatedSaveTime = initialSaveTime.plus(2.hours)
                val updatedSaveTime2 = initialSaveTime.plus(4.hours)
            }
        },
    ) exercise {
        clock.currentTime = initialSaveTime
        repository.save(partyId.with(pin))
        clock.currentTime = updatedSaveTime
        repository.save(partyId.with(updatedPin))
        clock.currentTime = updatedSaveTime2
        repository.deletePin(partyId, pin.id)
    } verifyWithWait {
        repository.getPinRecords(partyId)
            .assertContains(Record(partyId.with(pin), user.email, false, initialSaveTime))
            .assertContains(Record(partyId.with(updatedPin), user.email, false, updatedSaveTime))
            .assertContains(Record(partyId.with(updatedPin), user.email, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context {
                val partyId = stubPartyId()
                val records = listOf(
                    partyRecord(partyId, stubPin(), uuidString(), false, now().minus(3.months)),
                    partyRecord(partyId, stubPin(), uuidString(), true, now().minus(2.years)),
                )
            }
        },
    ) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verifyWithWait {
        val loadedRecords = repository.getPinRecords(partyId)
        records.forEach { loadedRecords.assertContains(it) }
    }
}

private typealias Context = RepositoryContext<DynamoPinRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoPinRepository(user.email, clock) }()
}
