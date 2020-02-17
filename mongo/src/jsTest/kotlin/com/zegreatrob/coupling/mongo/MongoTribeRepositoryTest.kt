package com.zegreatrob.coupling.mongo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json
import kotlin.random.Random
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoTribeRepositoryTest"

class MongoTribeRepositoryTest :
    TribeRepositoryValidator {

    override suspend fun withRepository(handler: suspend (TribeRepository) -> Unit) {
        withMongoRepository { handler(this) }
    }

    companion object {

        class MongoTribeRepositoryTestAnchor(override val clock: TimeProvider) : MongoTribeRepository, MonkToolkit {
            val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)
            override val userEmail: String = "user-${Random.nextInt(200)}"
        }

        private fun repositoryWithDb() = MongoTribeRepositoryTestAnchor(TimeProvider)

        private inline fun withMongoRepository(block: MongoTribeRepositoryTestAnchor.() -> Unit) {
            val repositoryWithDb = repositoryWithDb()
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.db.close()
            }
        }
    }

    @Test
    fun canLoadTribeFromOldSchema() = testAsync {
        withMongoRepository {
            setupAsync(object {
                val expectedTribe = Tribe(
                    id = TribeId("safety"),
                    pairingRule = PairingRule.LongestTime,
                    defaultBadgeName = "Default",
                    alternateBadgeName = "Alternate",
                    name = "Safety Dance"
                )
            }) {
                tribesCollection.insert(
                    json(
                        "pairingRule" to 1,
                        "defaultBadgeName" to "Default",
                        "alternateBadgeName" to "Alternate",
                        "name" to "Safety Dance",
                        "id" to "safety"
                    )
                ).unsafeCast<Promise<Unit>>().await()
                Unit
            } exerciseAsync {
                getTribe(expectedTribe.id)
            } verifyAsync { result ->
                result.assertIsEqualTo(expectedTribe)
            }
        }
    }

}