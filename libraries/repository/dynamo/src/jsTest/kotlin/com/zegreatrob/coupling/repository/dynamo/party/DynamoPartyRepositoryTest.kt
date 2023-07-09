package com.zegreatrob.coupling.repository.dynamo.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.dynamo.DynamoPartyRepository
import com.zegreatrob.coupling.repository.validation.ContextMint
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyRepositoryValidator
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.bind
import com.zegreatrob.coupling.repository.validation.verifyWithWaitAnd
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncTestTemplate
import korlibs.time.DateTime
import korlibs.time.days
import korlibs.time.hours
import korlibs.time.months
import korlibs.time.years
import kotlin.test.Test

typealias PartyMint = ContextMint<DynamoPartyRepository>

@Suppress("unused")
class DynamoPartyRepositoryTest : PartyRepositoryValidator<DynamoPartyRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<DynamoPartyRepository>>(sharedSetup = {
        val user = stubUser()
        val clock = MagicClock()
        val repository = DynamoPartyRepository(user.email, clock)
        SharedContextData(repository, clock, user)
    })

    @Test
    fun getPartyRecordsWillReturnAllRecordsForAllUsers() = repositorySetup.with(
        object : PartyMint() {
            val initialSaveTime = DateTime.now().minus(3.days)
            val tribe = stubPartyDetails()
            val updatedParty = tribe.copy(name = "CLONE!")
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val altParty = stubPartyDetails()
        }.bind(),
    ) exercise {
        clock.currentTime = initialSaveTime
        repository.save(tribe)
        repository.save(altParty)
        clock.currentTime = updatedSaveTime
        repository.save(updatedParty)
        repository.deleteIt(altParty.id)
    } verifyWithWaitAnd {
        repository.getPartyRecords()
            .assertContains(Record(tribe, user.email, false, initialSaveTime))
            .assertContains(Record(altParty, user.email, false, initialSaveTime))
            .assertContains(Record(updatedParty, user.email, false, updatedSaveTime))
            .assertContains(Record(altParty, user.email, true, updatedSaveTime))
    } teardown {
        repository.deleteIt(tribe.id)
        repository.deleteIt(altParty.id)
    }

    @Test
    fun canSaveRawRecord() = repositorySetup.with(
        object : PartyMint() {
            val records = listOf(
                Record(stubPartyDetails(), uuidString(), false, DateTime.now().minus(3.months)),
                Record(stubPartyDetails(), uuidString(), true, DateTime.now().minus(2.years)),
            )
        }.bind(),
    ) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verifyAnd {
        with(repository.getPartyRecords()) {
            records.forEach { assertContains(it) }
        }
    } teardown {
        records.forEach {
            repository.deleteIt(it.data.id)
        }
    }
}