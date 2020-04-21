package com.zegreatrob.coupling.dynamo.user

import com.benasher44.uuid.uuid4
import com.soywiz.klock.*
import com.zegreatrob.coupling.dynamo.DynamoUserRepository
import com.zegreatrob.coupling.dynamo.RepositoryContext
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync2
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
    fun getUserRecordsWillReturnAllRecordsForAllUsers() = setupAsync2(contextProvider = buildRepository { context ->
        object : Context by context {
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedUser = user.copy(authorizedTribeIds = setOf(TribeId("clone!")))
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val altUser = stubUser()
        }
    }) {
        clock.currentTime = initialSaveTime
        repository.save(user)
        repository.save(altUser)
        clock.currentTime = updatedSaveTime
        repository.save(updatedUser)
    } exercise {
        repository.getUserRecords()
    } verify { result ->
        result
            .assertContains(Record(user, user.id, false, initialSaveTime))
            .assertContains(Record(altUser, user.id, false, initialSaveTime))
            .assertContains(Record(updatedUser, user.id, false, updatedSaveTime))
    }

    @Test
    fun canSaveRawRecord() = setupAsync2(buildRepository { context ->
        object : Context by context {
            val records = listOf(
                Record(stubUser(), uuidString(), false, DateTime.now().minus(3.months)),
                Record(stubUser(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }
    }) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verify {
        with(repository.getUserRecords()) {
            records.forEach { assertContains(it) }
        }
    }
}

private typealias Context = RepositoryContext<DynamoUserRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend () -> T =
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoUserRepository(user.id, clock) }
