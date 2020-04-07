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
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

private const val mongoUrl = "localhost/UsersRepositoryTest"

@Suppress("unused")
class MongoUserRepositoryTest : UserRepositoryValidator {

    override suspend fun withRepository(clock: TimeProvider, handler: suspend (UserRepository, User) -> Unit) {
        val currentUser = User("${uuid4()}", "${uuid4()}", emptySet())
        withMongoRepository(currentUser.id, clock) {
            handler(it, currentUser)
        }
    }

    companion object {
        private fun repositoryWithDb(email: String, clock: TimeProvider) = MongoUserRepositoryTestAnchor(email, clock)

        class MongoUserRepositoryTestAnchor(override val userId: String, override val clock: TimeProvider) :
            MongoUserRepository,
            MonkToolkit {
            private val db = getDb(mongoUrl)
            private val jsRepository: dynamic = jsRepository(db)
            override val userCollection = jsRepository.userCollection
            fun close() = db.close()
        }

        private inline fun withMongoRepository(
            email: String,
            clock: TimeProvider,
            block: (MongoUserRepositoryTestAnchor) -> Unit
        ) {
            val repositoryWithDb = repositoryWithDb(email, clock)
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.close()
            }
        }
    }

    @Test
    fun getUserRecordsWillReturnAllRecordsForAllUsers() = testAsync {
        val user = stubUser()
        val clock = MagicClock()
        withMongoRepository(user.email, clock) { repository ->
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
                    .assertContains(Record(user, user.email, false, initialSaveTime))
                    .assertContains(Record(altUser, user.email, false, initialSaveTime))
                    .assertContains(Record(updatedUser, user.email, false, updatedSaveTime))
            }
        }
    }

}