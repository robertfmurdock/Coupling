package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubTribe
import stubUser
import uuidString
import kotlin.test.Test

@Suppress("unused")
class DynamoTribeRepositoryTest : TribeRepositoryValidator {
    override suspend fun withRepository(clock: TimeProvider, handler: suspend (TribeRepository, User) -> Unit) {
        val user = stubUser()
        val repository = DynamoTribeRepository(user.email, clock)
        handler(repository, user)
    }

    @Test
    fun getTribeRecordsWillReturnAllRecordsForAllUsers() = testAsync {
        val user = stubUser()
        val clock = MagicClock()
        val repository = DynamoTribeRepository(user.email, clock)
        setupAsync(object {
            val initialSaveTime = DateTime.now().minus(3.days)
            val tribe = stubTribe()
            val updatedTribe = tribe.copy(name = "CLONE!")
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val altTribe = stubTribe()
        }) {
            clock.currentTime = initialSaveTime
            repository.save(tribe)
            repository.save(altTribe)
            clock.currentTime = updatedSaveTime
            repository.save(updatedTribe)
            repository.delete(altTribe.id)
        } exerciseAsync {
            repository.getTribeRecords()
        } verifyAsync { result ->
            result
                .assertContains(Record(tribe, user.email, false, initialSaveTime))
                .assertContains(Record(altTribe, user.email, false, initialSaveTime))
                .assertContains(Record(updatedTribe, user.email, false, updatedSaveTime))
                .assertContains(Record(altTribe, user.email, true, updatedSaveTime))
        }
    }


    @Test
    fun canSaveRawRecord() = testAsync {
        val user = stubUser()
        val clock = MagicClock()
        val repository = DynamoTribeRepository(user.email, clock)
        setupAsync(object {
            val records = listOf(
                Record(stubTribe(), uuidString(), false, DateTime.now().minus(3.months)),
                Record(stubTribe(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }) exerciseAsync {
            records.forEach { repository.saveRawRecord(it) }
        } verifyAsync {
            with(repository.getTribeRecords()) {
                records.forEach { assertContains(it) }
            }
        }
    }

}