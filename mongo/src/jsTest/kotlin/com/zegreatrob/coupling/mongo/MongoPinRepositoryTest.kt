package com.zegreatrob.coupling.mongo

import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import stubPin
import kotlin.js.Promise
import kotlin.random.Random
import kotlin.test.Test

private const val mongoUrl = "localhost/PinsRepositoryTest"

class MongoPinRepositoryTest {
    companion object {
        private fun repositoryWithDb() = MongoPinRepositoryTestAnchor()

        class MongoPinRepositoryTestAnchor : MongoPinRepository, MonkToolkit {
            private val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)
            override val userEmail: String = "user-${Random.nextInt(200)}"

            suspend fun dropPins() = pinCollection.drop().unsafeCast<Promise<Unit>>().await()

            fun close() = db.close()
        }

        private inline fun withRepository(block: (MongoPinRepositoryTestAnchor) -> Unit) {
            val repositoryWithDb = repositoryWithDb()
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.close()
            }
        }
    }

    @Test
    fun canSaveAndGetPin() = testAsync {
        withRepository { repository ->
            repository.dropPins()
            setupAsync(object {
                val tribeId = TribeId("hoo")
                val pin = stubPin()
            }) exerciseAsync {
                repository.save(TribeIdPin(tribeId, pin))
                repository.getPins(tribeId)
            } verifyAsync { result ->
                result.assertContains(pin)
            }
        }
    }
}