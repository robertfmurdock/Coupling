package com.zegreatrob.coupling.repository.dynamo.tribe

import com.soywiz.klock.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.dynamo.DynamoTribeRepository
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

typealias TribeMint = ContextMint<DynamoTribeRepository>

@Suppress("unused")
class DynamoTribeRepositoryTest : TribeRepositoryValidator<DynamoTribeRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<DynamoTribeRepository>>(sharedSetup = {
        val user = stubUser()
        val clock = MagicClock()
        val repository = DynamoTribeRepository(user.email, clock)
        SharedContextData(repository, clock, user)
    })

    @Test
    fun getTribeRecordsWillReturnAllRecordsForAllUsers() = repositorySetup.with(object : TribeMint() {
        val initialSaveTime = DateTime.now().minus(3.days)
        val tribe = stubParty()
        val updatedTribe = tribe.copy(name = "CLONE!")
        val updatedSaveTime = initialSaveTime.plus(2.hours)
        val altTribe = stubParty()
    }.bind()) {
        clock.currentTime = initialSaveTime
        repository.save(tribe)
        repository.save(altTribe)
        clock.currentTime = updatedSaveTime
        repository.save(updatedTribe)
        repository.delete(altTribe.id)
    } exercise {
        repository.getTribeRecords()
    } verifyAnd { result ->
        result
            .assertContains(Record(tribe, user.email, false, initialSaveTime))
            .assertContains(Record(altTribe, user.email, false, initialSaveTime))
            .assertContains(Record(updatedTribe, user.email, false, updatedSaveTime))
            .assertContains(Record(altTribe, user.email, true, updatedSaveTime))
    } teardown {
        repository.delete(tribe.id)
        repository.delete(altTribe.id)
    }

    @Test
    fun canSaveRawRecord() = repositorySetup.with(object : TribeMint() {
        val records = listOf(
            Record(stubParty(), uuidString(), false, DateTime.now().minus(3.months)),
            Record(stubParty(), uuidString(), true, DateTime.now().minus(2.years))
        )
    }.bind()) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verifyAnd {
        with(repository.getTribeRecords()) {
            records.forEach { assertContains(it) }
        }
    } teardown {
        records.forEach {
            repository.delete(it.data.id)
        }
    }
}
