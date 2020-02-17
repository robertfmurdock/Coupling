package com.zegreatrob.coupling.mongo

import com.benasher44.uuid.uuid4
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.user.MongoUserRepository
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator

private const val mongoUrl = "localhost/UsersRepositoryTest"

@Suppress("unused")
class MongoUserRepositoryTest : UserRepositoryValidator {

    override suspend fun withRepository(clock: TimeProvider, handler: suspend (UserRepository, User) -> Unit) {
        val currentUser = User("${uuid4()}", "${uuid4()}", emptySet())
        withMongoRepository(currentUser.email, clock) {
            handler(it, currentUser)
        }
    }

    companion object {
        private fun repositoryWithDb(email: String, clock: TimeProvider) = MongoUserRepositoryTestAnchor(email, clock)

        class MongoUserRepositoryTestAnchor(override val userEmail: String, override val clock: TimeProvider) :
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

}