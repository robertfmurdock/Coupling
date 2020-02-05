package com.zegreatrob.coupling.mongo

import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import stubTribe
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.random.Random
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoTribeRepositoryTest"

class MongoTribeRepositoryTest {

    companion object {

        class MongoTribeRepositoryTestAnchor : MongoTribeRepository, MonkToolkit {
            val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)
            override val userEmail: String = "user-${Random.nextInt(200)}"

            suspend fun drop() {
                tribesCollection.drop().unsafeCast<Promise<Unit>>().await()
            }

            suspend fun getDbTribes(tribeId: TribeId) =
                tribesCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
        }

        private fun repositoryWithDb() = MongoTribeRepositoryTestAnchor()

        private inline fun withRepository(block: MongoTribeRepositoryTestAnchor.() -> Unit) {
            val repositoryWithDb = repositoryWithDb()
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.db.close()
            }
        }
    }

    @Test
    fun canSaveAndLoadTribe() = testAsync {
        withRepository {
            setupAsync(object {
                val tribe = Tribe(
                    id = TribeId(id()),
                    pairingRule = PairingRule.PreferDifferentBadge,
                    email = "safety@dance.edu",
                    badgesEnabled = true,
                    callSignsEnabled = true
                )
            }) {
                drop()
            } exerciseAsync {
                save(tribe)
                getTribe(tribe.id)
            } verifyAsync { result ->
                result.assertIsEqualTo(tribe)
            }
        }
    }

    @Test
    fun canSaveAndLoadVarietyOfTribes() = testAsync {
        withRepository {
            setupAsync(object {
                val tribes = listOf(
                    stubTribe(),
                    stubTribe(),
                    stubTribe()
                )
            }) exerciseAsync {
                tribes.forEach { save(it) }
                tribes.map { it.id }
                    .map { id -> getTribe(id) }
            } verifyAsync { loadedTribes ->
                loadedTribes.assertIsEqualTo(tribes)
            }
        }
    }

    @Test
    fun canLoadTribeFromOldSchema() = testAsync {
        withRepository {
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

    @Test
    fun willLoadAllTribes() = testAsync {
        withRepository {
            setupAsync(object {
                val tribes = listOf(
                    Tribe(
                        id = TribeId(id()),
                        pairingRule = PairingRule.PreferDifferentBadge,
                        name = "1"
                    ),
                    Tribe(
                        id = TribeId(id()),
                        pairingRule = PairingRule.LongestTime,
                        name = "2"
                    ),
                    Tribe(
                        id = TribeId(id()),
                        pairingRule = PairingRule.LongestTime,
                        name = "3"
                    )
                )
            }) {
                drop()
                tribes.forEach { save(it) }
            } exerciseAsync {
                getTribes()
            } verifyAsync { result ->
                result.assertIsEqualTo(tribes)
            }
        }
    }
}