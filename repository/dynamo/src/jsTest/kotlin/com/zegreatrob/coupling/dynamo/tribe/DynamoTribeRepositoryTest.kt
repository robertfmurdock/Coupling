package com.zegreatrob.coupling.dynamo.tribe

import com.soywiz.klock.*
import com.zegreatrob.coupling.dynamo.DynamoTribeRepository
import com.zegreatrob.coupling.dynamo.RepositoryContext
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync2
import kotlin.test.Test

@Suppress("unused")
class DynamoTribeRepositoryTest : TribeRepositoryValidator {
    override suspend fun withRepository(clock: TimeProvider, handler: suspend (TribeRepository, User) -> Unit) {
        val user = stubUser()
        val repository = DynamoTribeRepository(user.email, clock)
        handler(repository, user)
    }

    @Test
    fun getTribeRecordsWillReturnAllRecordsForAllUsers() = setupAsync2(contextProvider = buildRepository { context ->
        object : Context by context {
            val initialSaveTime = DateTime.now().minus(3.days)
            val tribe = stubTribe()
            val updatedTribe = tribe.copy(name = "CLONE!")
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val altTribe = stubTribe()
        }
    }) {
        clock.currentTime = initialSaveTime
        repository.save(tribe)
        repository.save(altTribe)
        clock.currentTime = updatedSaveTime
        repository.save(updatedTribe)
        repository.delete(altTribe.id)
    } exercise {
        repository.getTribeRecords()
    } verify { result ->
        result
            .assertContains(Record(tribe, user.email, false, initialSaveTime))
            .assertContains(Record(altTribe, user.email, false, initialSaveTime))
            .assertContains(Record(updatedTribe, user.email, false, updatedSaveTime))
            .assertContains(Record(altTribe, user.email, true, updatedSaveTime))
    }

    @Test
    fun canSaveRawRecord() = setupAsync2(buildRepository { context ->
        object : Context by context {
            val records = listOf(
                Record(stubTribe(), uuidString(), false, DateTime.now().minus(3.months)),
                Record(stubTribe(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }
    }) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verify {
        with(repository.getTribeRecords()) {
            records.forEach { assertContains(it) }
        }
    }
}

private typealias Context = RepositoryContext<DynamoTribeRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend () -> T =
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoTribeRepository(user.email, clock) }
