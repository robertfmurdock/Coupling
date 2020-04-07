package com.zegreatrob.coupling.dynamo

import com.benasher44.uuid.uuid4
import com.soywiz.klock.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test

@Suppress("unused")
class DynamoUserRepositoryTest : UserRepositoryValidator {
    override suspend fun withRepository(clock: TimeProvider, handler: suspend (UserRepository, User) -> Unit) {
        withDynamoRepository(clock, handler)
    }

    private suspend fun withDynamoRepository(
        clock: TimeProvider = TimeProvider,
        handler: suspend (DynamoUserRepository, User) -> Unit
    ) {
        val userId = "${uuid4()}"
        val user = User(userId, "${uuid4()}", emptySet())
        handler(DynamoUserRepository(userId, clock), user)
    }

    private fun testDynamoRepository(
        block: suspend CoroutineScope.(DynamoUserRepository, User, MagicClock) -> Any?
    ) = testAsync {
        val clock = MagicClock()
        withDynamoRepository(clock) { repository, user -> block(repository, user, clock) }
    }

    @Test
    fun getUserRecordsWillReturnAllRecordsForAllUsers() = testDynamoRepository { repository, user, clock ->
        setupAsync(object {
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedUser = user.copy(authorizedTribeIds = setOf(TribeId("clone!")))
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val altUser = stubUser()
        }) {
            clock.currentTime = initialSaveTime
            repository.save(user)
            repository.save(altUser)
            clock.currentTime = updatedSaveTime
            repository.save(updatedUser)
        } exerciseAsync {
            repository.getUserRecords()
        } verifyAsync { result ->
            result
                .assertContains(Record(user, user.id, false, initialSaveTime))
                .assertContains(Record(altUser, user.id, false, initialSaveTime))
                .assertContains(Record(updatedUser, user.id, false, updatedSaveTime))
        }
    }

    @Test
    fun canSaveRawRecord() = testDynamoRepository { repository, _, _ ->
        setupAsync(object {
            val records = listOf(
                Record(stubUser(), uuidString(), false, DateTime.now().minus(3.months)),
                Record(stubUser(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }) exerciseAsync {
            records.forEach { repository.saveRawRecord(it) }
        } verifyAsync {
            with(repository.getUserRecords()) {
                records.forEach { assertContains(it) }
            }
        }
    }
}
