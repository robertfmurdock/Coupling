package com.zegreatrob.coupling.mongo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import stubUser
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoTribeRepositoryTest"

class MongoTribeRepositoryTest : TribeRepositoryValidator {

    override suspend fun withRepository(clock: TimeProvider, handler: suspend (TribeRepository, User) -> Unit) {
        val user = stubUser()
        withMongoRepository(user, clock) { handler(this, user) }
    }

    companion object {

        class MongoTribeRepositoryTestAnchor(
            override val userEmail: String,
            override val clock: TimeProvider
        ) : MongoTribeRepository, MonkToolkit {
            val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)
        }

        private fun repositoryWithDb(user: User, clock: TimeProvider) =
            MongoTribeRepositoryTestAnchor(user.email, clock)

        private inline fun withMongoRepository(
            user: User = stubUser(),
            clock: TimeProvider = TimeProvider,
            block: MongoTribeRepositoryTestAnchor.() -> Unit
        ) {
            val repositoryWithDb = repositoryWithDb(user, clock)
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