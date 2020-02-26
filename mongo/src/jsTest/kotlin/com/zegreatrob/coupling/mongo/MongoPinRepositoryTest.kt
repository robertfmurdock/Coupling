package com.zegreatrob.coupling.mongo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import kotlinx.coroutines.await
import stubPin
import stubTribeId
import stubUser
import kotlin.js.Promise
import kotlin.test.Test

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

    @Test
    fun saveThenDeleteWith12CharacterStringPinIdWillWorkCorrectly() = testRepository { repository, tribeId, _, _ ->
        setupAsync(object {
            val pin = stubPin().copy(_id = "19377906-pin")
        }) {
            repository.save(tribeId.with(pin))
        } exerciseAsync {
            repository.deletePin(tribeId, pin._id!!)
            repository.getPins(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
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