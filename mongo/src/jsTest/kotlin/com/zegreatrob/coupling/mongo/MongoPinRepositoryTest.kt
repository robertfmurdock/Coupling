package com.zegreatrob.coupling.mongo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import kotlinx.coroutines.await
import stubTribeId
import kotlin.js.Promise
import kotlin.random.Random

private const val mongoUrl = "localhost/PinsRepositoryTest"

@Suppress("unused")
class MongoPinRepositoryTest : PinRepositoryValidator {

    override suspend fun withRepository(handler: suspend (PinRepository, TribeId) -> Unit) {
        withMongoRepository { handler(it, stubTribeId()) }
    }

    companion object {
        private fun repositoryWithDb() = MongoPinRepositoryTestAnchor(TimeProvider)

        class MongoPinRepositoryTestAnchor(override val clock: TimeProvider) : MongoPinRepository, MonkToolkit {
            private val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)
            override val userEmail: String = "user-${Random.nextInt(200)}"

            suspend fun dropPins() = pinCollection.drop().unsafeCast<Promise<Unit>>().await()

            fun close() = db.close()
        }

        private inline fun withMongoRepository(block: (MongoPinRepositoryTestAnchor) -> Unit) {
            val repositoryWithDb = repositoryWithDb()
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.close()
            }
        }
    }

}