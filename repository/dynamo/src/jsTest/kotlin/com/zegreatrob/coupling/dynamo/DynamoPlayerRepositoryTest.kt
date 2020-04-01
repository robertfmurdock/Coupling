package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import kotlin.test.Test

@Suppress("unused")
class DynamoPlayerRepositoryTest : PlayerEmailRepositoryValidator<DynamoPlayerRepository> {

    override suspend fun withRepository(
        clock: MagicClock,
        handler: suspend (DynamoPlayerRepository, TribeId, User) -> Unit
    ) {
        val user = stubUser()
        handler(DynamoPlayerRepository(user.email, clock), stubTribeId(), user)
    }

    @Test
    fun getPlayerRecordsWillShowAllRecordsIncludingDeletions() = testAsync {
        val clock = MagicClock()
        val user = stubUser()
        val repository = DynamoPlayerRepository(user.email, clock)
        setupAsync(object {
            val tribeId = stubTribeId()
            val player = stubPlayer()
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedPlayer = player.copy(name = "CLONE")
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val updatedSaveTime2 = initialSaveTime.plus(4.hours)
        }) {
            clock.currentTime = initialSaveTime
            repository.save(tribeId.with(player))
            clock.currentTime = updatedSaveTime
            repository.save(tribeId.with(updatedPlayer))
            clock.currentTime = updatedSaveTime2
            repository.deletePlayer(tribeId, player.id!!)
        } exerciseAsync {
            repository.getPlayerRecords(tribeId)
        } verifyAsync { result ->
            result
                .assertContains(Record(tribeId.with(player), user.email, false, initialSaveTime))
                .assertContains(Record(tribeId.with(updatedPlayer), user.email, false, updatedSaveTime))
                .assertContains(Record(tribeId.with(updatedPlayer), user.email, true, updatedSaveTime2))
        }
    }

    @Test
    fun canSaveRawRecord() = testAsync {
        val clock = MagicClock()
        val user = stubUser()
        val repository = DynamoPlayerRepository(user.email, clock)

        setupAsync(object {
            val tribeId = stubTribeId()
            val records = listOf(
                tribeRecord(tribeId, stubPlayer(), uuidString(), false, DateTime.now().minus(3.months)),
                tribeRecord(tribeId, stubPlayer(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }) exerciseAsync {
            records.forEach { repository.saveRawRecord(it) }
        } verifyAsync {
            with(repository.getPlayerRecords(tribeId)) {
                records.forEach { assertContains(it) }
            }
        }
    }

}