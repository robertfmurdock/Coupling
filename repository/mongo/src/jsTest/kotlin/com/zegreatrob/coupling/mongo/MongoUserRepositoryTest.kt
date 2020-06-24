package com.zegreatrob.coupling.mongo

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.user.MongoUserRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

private const val mongoUrl = "localhost/UsersRepositoryTest"

@Suppress("unused")
class MongoUserRepositoryTest :
    UserRepositoryValidator<MongoUserRepositoryTest.Companion.MongoUserRepositoryTestAnchor> {

    companion object {
        class MongoUserRepositoryTestAnchor(override val userId: String, override val clock: TimeProvider) :
            MongoUserRepository,
            MonkToolkit {
            private val db = getDb(mongoUrl)
            private val jsRepository: dynamic = jsRepository(db)
            override val userCollection = jsRepository.userCollection
            fun close(): Unit = db.close().unsafeCast<Unit>()
        }
    }

    override val repositorySetup = asyncTestTemplate<SharedContext<MongoUserRepositoryTestAnchor>> { test ->
        val clock = MagicClock()
        val currentUser = User("${uuid4()}", "${uuid4()}", emptySet())
        val repo = MongoUserRepositoryTestAnchor(currentUser.id, clock)
        test(SharedContextData(repo, clock, currentUser))
        repo.close()
    }

    @Test
    fun getUserRecordsWillReturnAllRecordsForAllUsers() = repositorySetup({
        object : SharedContext<MongoUserRepositoryTestAnchor> by it {
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedUser by lazy { user.copy(authorizedTribeIds = setOf(TribeId("clone!"))) }
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

}