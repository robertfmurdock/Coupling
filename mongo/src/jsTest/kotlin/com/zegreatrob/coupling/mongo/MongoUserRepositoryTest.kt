package com.zegreatrob.coupling.mongo

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.user.MongoUserRepository
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repositoryvalidation.UserRepositoryValidator

private const val mongoUrl = "localhost/UsersRepositoryTest"

@Suppress("unused")
class MongoUserRepositoryTest : UserRepositoryValidator {

    override suspend fun withRepository(handler: suspend (UserRepository, User) -> Unit) {
        val currentUser = User("${uuid4()}", emptySet())
        withMongoRepository(currentUser.email) {
            handler(it, currentUser)
        }
    }

    companion object {
        private fun repositoryWithDb(email: String) = MongoPinRepositoryTestAnchor(email)

        class MongoPinRepositoryTestAnchor(override val userEmail: String) : MongoUserRepository, MonkToolkit {
            private val db = getDb(mongoUrl)
            private val jsRepository: dynamic = jsRepository(db)
            override val userCollection = jsRepository.userCollection
            fun close() = db.close()
        }

        private inline fun withMongoRepository(email: String, block: (MongoPinRepositoryTestAnchor) -> Unit) {
            val repositoryWithDb = repositoryWithDb(email)
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.close()
            }
        }
    }

}