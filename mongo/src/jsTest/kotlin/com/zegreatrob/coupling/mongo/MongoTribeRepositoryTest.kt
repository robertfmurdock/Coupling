package com.zegreatrob.coupling.mongo

import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json
import kotlin.random.Random
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoTribeRepositoryTest"

class MongoTribeRepositoryTest {

    companion object : MongoTribeRepository, MonkToolkit {
        override val userEmail: String = "user-${Random.nextInt(200)}"
        override val jsRepository: dynamic = jsRepository(mongoUrl)
        private val tribeCollection by lazy<dynamic> { getCollection("tribes", mongoUrl) }

        suspend fun dropPlayers() {
            tribeCollection.drop().unsafeCast<Promise<Unit>>().await()
        }
    }

    @Test
    fun canSaveAndLoadTribe() = testAsync {
        setupAsync(object {
            val tribe = KtTribe(
                id = TribeId(id()),
                pairingRule = PairingRule.PreferDifferentBadge,
                email = "safety@dance.edu",
                badgesEnabled = true,
                callSignsEnabled = true
            )
        }) {
            dropPlayers()
        } exerciseAsync {
            save(tribe)
            getTribeAsync(tribe.id).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(tribe)
        }
    }

    @Test
    fun canLoadTribeFromOldSchema() = testAsync {
        setupAsync(object {
            val expectedTribe = KtTribe(
                id = TribeId("safety"),
                pairingRule = PairingRule.LongestTime,
                defaultBadgeName = "Default",
                alternateBadgeName = "Alternate",
                name = "Safety Dance"
            )
        }) {
            tribeCollection.insert(json(
                    "pairingRule" to 1,
                    "defaultBadgeName" to "Default",
                    "alternateBadgeName" to "Alternate",
                    "name" to "Safety Dance",
                    "id" to "safety"
            )).unsafeCast<Promise<Unit>>().await()
            Unit
        } exerciseAsync {
            getTribeAsync(expectedTribe.id).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(expectedTribe)
        }
    }

    @Test
    fun willLoadAllTribes() = testAsync {
        setupAsync(object {
            val tribes = listOf(
                KtTribe(
                    id = TribeId(id()),
                    pairingRule = PairingRule.PreferDifferentBadge,
                    name = "1"
                ),
                KtTribe(
                    id = TribeId(id()),
                    pairingRule = PairingRule.LongestTime,
                    name = "2"
                ),
                KtTribe(
                    id = TribeId(id()),
                    pairingRule = PairingRule.LongestTime,
                    name = "3"
                )
            )
        }) {
            dropPlayers()
            tribes.forEach { save(it) }
        } exerciseAsync {
            getTribesAsync().await()
        } verifyAsync { result ->
            result.assertIsEqualTo(tribes)
        }
    }
}