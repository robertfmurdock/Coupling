package com.zegreatrob.coupling.repository.dynamo.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.dynamo.DynamoPartyRepository
import com.zegreatrob.coupling.repository.dynamo.now
import com.zegreatrob.coupling.repository.dynamo.pairs.months
import com.zegreatrob.coupling.repository.dynamo.pairs.years
import com.zegreatrob.coupling.repository.validation.ContextMint
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyRepositoryValidator
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.bind
import com.zegreatrob.coupling.repository.validation.verifyWithWaitAnd
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

typealias PartyMint = ContextMint<DynamoPartyRepository>

@Suppress("unused")
class DynamoPartyRepositoryTest : PartyRepositoryValidator<DynamoPartyRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<DynamoPartyRepository>>(sharedSetup = {
        val user = stubUserDetails()
        val clock = MagicClock()
        val repository = DynamoPartyRepository(user.id, clock)
        SharedContextData(repository, clock, user)
    })

    @Test
    fun getPartyRecordsWillReturnAllRecordsForAllUsers() = repositorySetup.with(
        object : PartyMint() {
            val initialSaveTime = now().minus(3.days)
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
            .assertContains(Record(tribe, user.id.value, false, initialSaveTime))
            .assertContains(Record(altParty, user.id.value, false, initialSaveTime))
            .assertContains(Record(updatedParty, user.id.value, false, updatedSaveTime))
            .assertContains(Record(altParty, user.id.value, true, updatedSaveTime))
    } teardown {
        repository.deleteIt(tribe.id)
        repository.deleteIt(altParty.id)
    }

    @Test
    fun canSaveRawRecord() = repositorySetup.with(
        object : PartyMint() {
            val records = listOf(
                Record(stubPartyDetails(), uuidString().toNotBlankString().getOrThrow(), false, now().minus(3.months)),
                Record(stubPartyDetails(), uuidString().toNotBlankString().getOrThrow(), true, now().minus(2.years)),
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
