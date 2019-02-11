package com.zegreatrob.coupling.server.entity.tribe

import assertIsEqualTo
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.PairingRule
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.entity.tribe.MongoTribeRepository
import com.zegreatrob.coupling.server.MonkToolkit
import com.zegreatrob.coupling.server.UserContext
import exerciseAsync
import kotlinx.coroutines.await
import setupAsync
import testAsync
import verifyAsync
import kotlin.js.Promise
import kotlin.js.json
import kotlin.random.Random
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoTribeRepositoryTest"

class MongoTribeRepositoryTest {

    companion object : MongoTribeRepository, MonkToolkit {
        override val userContext = object : UserContext {
            override val username: String
                get() = "User-${Random.nextInt(200)}"

        }
        override val jsRepository: dynamic = jsRepository(mongoUrl)
        private val tribeCollection by lazy<dynamic> { getCollection("tribes", mongoUrl) }

        suspend fun dropPlayers() {
            tribeCollection.drop().unsafeCast<Promise<Unit>>().await()
        }
    }

    @Test
    fun canSaveAndLoadTribe() = testAsync {
        setupAsync(object {
            val tribe = KtTribe(TribeId(id()), PairingRule.PreferDifferentBadge)
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
            ))
            Unit
        } exerciseAsync {
            getTribeAsync(expectedTribe.id).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(expectedTribe)
        }
    }
}