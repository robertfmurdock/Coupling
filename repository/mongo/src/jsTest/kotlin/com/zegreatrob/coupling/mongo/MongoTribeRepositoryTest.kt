package com.zegreatrob.coupling.mongo

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoTribeRepositoryTest"

typealias MongoTribeMint = ContextMint<MongoTribeRepositoryTest.Companion.MongoTribeRepositoryTestAnchor>

class MongoTribeRepositoryTest :
    TribeRepositoryValidator<MongoTribeRepositoryTest.Companion.MongoTribeRepositoryTestAnchor> {

    override val repositorySetup = asyncTestTemplate<SharedContext<MongoTribeRepositoryTestAnchor>>(wrapper = {
        val user = stubUser()
        val clock = MagicClock()
        withMongoRepository(user, clock) {
            it(SharedContextData(this, clock, user))
        }
    })

    @Test
    fun getTribeRecordListWillIncludeAllSavesOfTribeIncludingDelete() = repositorySetup(object : MongoTribeMint() {
        val tribe = stubTribe()
        val updatedTribe = tribe.copy(name = "CLONE")
        val firstSaveTime = DateTime.now().minus(3.days)
        val secondSaveTime = firstSaveTime.plus(3.days)
    }.bind()) {
        clock.currentTime = firstSaveTime
        repository.save(tribe)
        clock.currentTime = secondSaveTime
        repository.save(updatedTribe)
        repository.delete(tribe.id)
    } exercise {
        repository.getTribeRecordList()
    } verify { result ->
        result.filter { it.data.id == tribe.id }.let {
            it[0].apply {
                modifyingUserId.assertIsEqualTo(user.email)
                timestamp.assertIsEqualTo(firstSaveTime)
                isDeleted.assertIsEqualTo(false)
            }
            it[1].apply {
                modifyingUserId.assertIsEqualTo(user.email)
                timestamp.assertIsEqualTo(secondSaveTime)
                isDeleted.assertIsEqualTo(false)
            }
            it[2].apply {
                modifyingUserId.assertIsEqualTo(user.email)
                timestamp.assertIsEqualTo(secondSaveTime)
                isDeleted.assertIsEqualTo(true)
            }
        }
    }

    companion object {

        class MongoTribeRepositoryTestAnchor(
            override val userId: String,
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
    fun canLoadTribeFromOldSchema() = repositorySetup(object : MongoTribeMint() {
        val expectedTribe = Tribe(
            id = TribeId("safety"),
            pairingRule = PairingRule.LongestTime,
            defaultBadgeName = "Default",
            alternateBadgeName = "Alternate",
            name = "Safety Dance"
        )
    }.bind()) {
        repository.tribesCollection.insert(
            json(
                "pairingRule" to 1,
                "defaultBadgeName" to "Default",
                "alternateBadgeName" to "Alternate",
                "name" to "Safety Dance",
                "id" to "safety"
            )
        ).unsafeCast<Promise<Unit>>().await()
        Unit
    } exercise {
        repository.getTribeRecord(expectedTribe.id)
    } verify { result ->
        result?.data
            .assertIsEqualTo(expectedTribe)
    }

}