package com.zegreatrob.coupling.repository.dynamo.player

import com.soywiz.klock.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.repository.dynamo.DynamoPlayerRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoRecordJsonMapping
import com.zegreatrob.coupling.repository.dynamo.RepositoryContext
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.js.json
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@Suppress("unused")
@ExperimentalTime
class DynamoPlayerRepositoryTest : PlayerEmailRepositoryValidator<DynamoPlayerRepository> {

    override val repositorySetup = asyncTestTemplate<TribeContext<DynamoPlayerRepository>>(sharedSetup = {
        val user = stubUser()
        val clock = MagicClock()
        val repo = DynamoPlayerRepository(user.email, clock)
        object : TribeContext<DynamoPlayerRepository> {
            override val tribeId = stubPartyId()
            override val clock = clock
            override val user = user
            override val repository = repo
        }
    })

    @Test
    fun getPlayerRecordsWillShowAllRecordsIncludingDeletions() = asyncSetup.with(buildRepository { context ->
        object : Context by context {
            val tribeId = stubPartyId()
            val player = stubPlayer()
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedPlayer = player.copy(name = "CLONE")
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val updatedSaveTime2 = initialSaveTime.plus(4.hours)
        }
    }, additionalActions = {
        clock.currentTime = initialSaveTime
        repository.save(tribeId.with(player))
        clock.currentTime = updatedSaveTime
        repository.save(tribeId.with(updatedPlayer))
        clock.currentTime = updatedSaveTime2
        repository.deletePlayer(tribeId, player.id)
    }) exercise {
        repository.getPlayerRecords(tribeId)
    } verify { result ->
        result
            .assertContains(Record(tribeId.with(player), user.email, false, initialSaveTime))
            .assertContains(Record(tribeId.with(updatedPlayer), user.email, false, updatedSaveTime))
            .assertContains(Record(tribeId.with(updatedPlayer), user.email, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup.with(buildRepository { context ->
        object : Context by context {
            val tribeId = stubPartyId()
            val records = listOf(
                tribeRecord(tribeId, stubPlayer(), uuidString(), false, DateTime.now().minus(3.months)),
                tribeRecord(tribeId, stubPlayer(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }
    }) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verify {
        with(repository.getPlayerRecords(tribeId)) {
            records.forEach { assertContains(it) }
        }
    }

    @Test
    fun getPlayerRecordsWillIgnorePlayerRecordsWithoutId() = asyncSetup.with(buildRepository { context ->
        object : Context by context, DynamoRecordJsonMapping {
            val tribeId = stubPartyId()
            override val userId: String = "test user"
        }
    }) {
        DynamoPlayerRepository.performPutItem(
            recordJson(DateTime.now())
                .add(
                    json(
                        "tribeId" to tribeId.value,
                        "timestamp+id" to "lol",
                        "name" to "Dead player"
                    )
                )
        )
    } exercise {
        repository.getPlayerRecords(tribeId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun getPlayersWillIgnorePlayerRecordsWithout() = asyncSetup.with(buildRepository { context ->
        object : Context by context, DynamoRecordJsonMapping {
            val tribeId = stubPartyId()
            override val userId: String = "test user"
        }
    }) {
        DynamoPlayerRepository.performPutItem(
            recordJson(DateTime.now())
                .add(
                    json(
                        "tribeId" to tribeId.value,
                        "timestamp+id" to "20210426135844.172+",
                        "name" to "Dead player"
                    )
                )
        )
    } exercise {
        repository.getPlayers(tribeId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

}

private typealias Context = RepositoryContext<DynamoPlayerRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoPlayerRepository(user.email, clock) }()
}
