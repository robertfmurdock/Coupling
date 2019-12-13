package com.zegreatrob.coupling.mongo

import com.zegreatrob.coupling.model.tribe.Tribe
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
import kotlin.test.BeforeTest
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoTribeRepositoryTest"

private external interface Database {
    fun close()
}

class MongoTribeRepositoryTest {

    lateinit var repository: MongoTribeRepository
    lateinit var toolkit: MonkToolkit
    private var tribeCollection: dynamic = null
    private lateinit var db: Database

    @BeforeTest
    fun setup() {
        val thing = object : MongoTribeRepository, MonkToolkit {
            val db = getDb(mongoUrl).unsafeCast<Database>()
            override val userEmail: String = "user-${Random.nextInt(200)}"
            override val jsRepository: dynamic = jsRepository(db)
        }
        db = thing.db
        repository = thing
        toolkit = thing
        tribeCollection = with(thing) { getCollection("tribes", db) }
    }

    private suspend fun dropPlayers() {
        tribeCollection.drop().unsafeCast<Promise<Unit>>().await()
    }

    @Test
    fun canSaveAndLoadTribe() = testAsync {
        setupAsync(object : MongoTribeRepository by repository, MonkToolkit by toolkit {
            val tribe = Tribe(
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
            getTribe(tribe.id)
        } verifyAsync { result ->
            result.assertIsEqualTo(tribe)
        }
        db.close()
    }

    @Test
    fun canLoadTribeFromOldSchema() = testAsync {
        setupAsync(object : MongoTribeRepository by repository, MonkToolkit by toolkit {
            val expectedTribe = Tribe(
                id = TribeId("safety"),
                pairingRule = PairingRule.LongestTime,
                defaultBadgeName = "Default",
                alternateBadgeName = "Alternate",
                name = "Safety Dance"
            )
        }) {
            tribeCollection.insert(
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
        db.close()
    }

    @Test
    fun willLoadAllTribes() = testAsync {
        setupAsync(object : MongoTribeRepository by repository, MonkToolkit by toolkit {
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
            dropPlayers()
            tribes.forEach { save(it) }
        } exerciseAsync {
            getTribes()
        } verifyAsync { result ->
            result.assertIsEqualTo(tribes)
        }
        db.close()
    }
}