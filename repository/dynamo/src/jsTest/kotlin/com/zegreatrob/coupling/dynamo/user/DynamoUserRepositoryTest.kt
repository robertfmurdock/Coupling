package com.zegreatrob.coupling.dynamo.user

import com.benasher44.uuid.uuid4
import com.soywiz.klock.*
import com.zegreatrob.coupling.dynamo.DynamoUserRepository
import com.zegreatrob.coupling.dynamo.RepositoryContext
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

@Suppress("unused")
class DynamoUserRepositoryTest : UserRepositoryValidator<DynamoUserRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<DynamoUserRepository>>(sharedSetup = {
        val clock = MagicClock()
        val userId = "${uuid4()}"
        val user = User(userId, "${uuid4()}", emptySet())
        val repository = DynamoUserRepository(userId, clock)
        SharedContextData(repository, clock, user)
    })

    @Test
    fun getUserRecordsWillReturnAllRecordsForAllUsers() = asyncSetup(buildRepository { context ->
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
    fun canSaveRawRecord() = asyncSetup(buildRepository { context ->
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

private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoUserRepository(user.id, clock) }()
}
