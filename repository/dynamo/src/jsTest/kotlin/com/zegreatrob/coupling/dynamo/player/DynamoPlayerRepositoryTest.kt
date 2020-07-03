package com.zegreatrob.coupling.dynamo.player

import com.soywiz.klock.*
import com.zegreatrob.coupling.dynamo.DynamoPlayerRepository
import com.zegreatrob.coupling.dynamo.RepositoryContext
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

@Suppress("unused")
class DynamoPlayerRepositoryTest : PlayerEmailRepositoryValidator<DynamoPlayerRepository> {

    override val repositorySetup = asyncTestTemplate<TribeContext<DynamoPlayerRepository>>(sharedSetup = {
        val user = stubUser()
        val clock = MagicClock()
        val repo = DynamoPlayerRepository(user.email, clock)
        object : TribeContext<DynamoPlayerRepository> {
            override val tribeId = stubTribeId()
            override val clock = clock
            override val user = user
            override val repository = repo
        }
    })

    @Test
    fun getPlayerRecordsWillShowAllRecordsIncludingDeletions() = asyncSetup(buildRepository { context ->
        object : Context by context {
            val tribeId = stubTribeId()
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
        repository.deletePlayer(tribeId, player.id!!)
    }) exercise {
        repository.getPlayerRecords(tribeId)
    } verify { result ->
        result
            .assertContains(Record(tribeId.with(player), user.email, false, initialSaveTime))
            .assertContains(Record(tribeId.with(updatedPlayer), user.email, false, updatedSaveTime))
            .assertContains(Record(tribeId.with(updatedPlayer), user.email, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup(buildRepository { context ->
        object : Context by context {
            val tribeId = stubTribeId()
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

}

private typealias Context = RepositoryContext<DynamoPlayerRepository>

private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
    RepositoryContext.buildRepository(setupContext) { user, clock -> DynamoPlayerRepository(user.email, clock) }()
}
