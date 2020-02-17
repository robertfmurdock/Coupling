package com.zegreatrob.coupling.mongo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import kotlinx.coroutines.await
import stubTribeId
import stubUser
import kotlin.js.Promise

private const val mongoUrl = "localhost/PinsRepositoryTest"

@Suppress("unused")
class MongoPinRepositoryTest : PinRepositoryValidator {

    override suspend fun withRepository(
        clock: TimeProvider,
        handler: suspend (PinRepository, TribeId, User) -> Unit
    ) {
        val user = stubUser()
        withMongoRepository(clock, user) { handler(it, stubTribeId(), user) }
    }

    companion object {
        private fun repositoryWithDb(
            clock: TimeProvider,
            user: User
        ) = MongoPinRepositoryTestAnchor(clock, user.email)

        class MongoPinRepositoryTestAnchor(override val clock: TimeProvider, override val userEmail: String) :
            MongoPinRepository, MonkToolkit {
            private val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)

            suspend fun dropPins() = pinCollection.drop().unsafeCast<Promise<Unit>>().await()

            fun close() = db.close()
        }

        private inline fun withMongoRepository(
            clock: TimeProvider,
            user: User,
            block: (MongoPinRepositoryTestAnchor) -> Unit
        ) {
            val repositoryWithDb = repositoryWithDb(clock, user)
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.close()
            }
        }
    }

}