package com.zegreatrob.coupling.repository.dynamo.player

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.months
import com.soywiz.klock.years
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.repository.dynamo.DynamoPlayerRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoRecordJsonMapping
import com.zegreatrob.coupling.repository.dynamo.RepositoryContext
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.js.json
import kotlin.test.Test

@Suppress("unused")
class DynamoPlayerRepositoryTest : PlayerEmailRepositoryValidator<DynamoPlayerRepository> {

    override val repositorySetup = asyncTestTemplate<PartyContext<DynamoPlayerRepository>>(sharedSetup = {
        val user = stubUser()
        val clock = MagicClock()
        val repo = DynamoPlayerRepository(user.email, clock)
        object : PartyContext<DynamoPlayerRepository> {
            override val partyId = stubPartyId()
            override val clock = clock
            override val user = user
            override val repository = repo
        }
    })

    @Test
    fun getPlayerRecordsWillShowAllRecordsIncludingDeletions() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context {
                val partyId = stubPartyId()
                val player = stubPlayer()
                val initialSaveTime = DateTime.now().minus(3.days)
                val updatedPlayer = player.copy(name = "CLONE")
                val updatedSaveTime = initialSaveTime.plus(2.hours)
                val updatedSaveTime2 = initialSaveTime.plus(4.hours)
            }
        },
        additionalActions = {
            clock.currentTime = initialSaveTime
            repository.save(partyId.with(player))
            clock.currentTime = updatedSaveTime
            repository.save(partyId.with(updatedPlayer))
            clock.currentTime = updatedSaveTime2
            repository.deletePlayer(partyId, player.id)
        },
    ) exercise {
        repository.getPlayerRecords(partyId)
    } verify { result ->
        result
            .assertContains(Record(partyId.with(player), user.email, false, initialSaveTime))
            .assertContains(Record(partyId.with(updatedPlayer), user.email, false, updatedSaveTime))
            .assertContains(Record(partyId.with(updatedPlayer), user.email, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context {
                val partyId = stubPartyId()
                val records = listOf(
                    partyRecord(partyId, stubPlayer(), uuidString(), false, DateTime.now().minus(3.months)),
                    partyRecord(partyId, stubPlayer(), uuidString(), true, DateTime.now().minus(2.years)),
                )
            }
        },
    ) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verify {
        with(repository.getPlayerRecords(partyId)) {
            records.forEach { assertContains(it) }
        }
    }

    @Test
    fun getPlayerRecordsWillIgnorePlayerRecordsWithoutId() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context, DynamoRecordJsonMapping {
                val partyId = stubPartyId()
                override val userId: String = "test user"
            }
        },
    ) {
        DynamoPlayerRepository.performPutItem(
            recordJson(DateTime.now())
                .add(
                    json(
                        "tribeId" to partyId.value,
                        "timestamp+id" to "lol",
                        "name" to "Dead player",
                    ),
                ),
        )
    } exercise {
        repository.getPlayerRecords(partyId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun getPlayersWillIgnorePlayerRecordsWithout() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context, DynamoRecordJsonMapping {
                val partyId = stubPartyId()
                override val userId: String = "test user"
            }
        },
    ) {
        DynamoPlayerRepository.performPutItem(
            recordJson(DateTime.now())
                .add(
                    json(
                        "tribeId" to partyId.value,
                        "timestamp+id" to "20210426135844.172+",
                        "name" to "Dead player",
                    ),
                ),
        )
    } exercise {
        repository.getPlayers(partyId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }
}

private typealias Context = RepositoryContext<DynamoPlayerRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoPlayerRepository(user.email, clock) }()
}
