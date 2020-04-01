package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubPairAssignmentDoc
import stubPlayer
import stubTribeId
import stubUser
import uuidString
import kotlin.test.Test

@Suppress("unused")
class DynamoPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator {
    override suspend fun withRepository(
        clock: TimeProvider,
        handler: suspend (PairAssignmentDocumentRepository, TribeId, User) -> Unit
    ) {
        val user = stubUser()
        handler(DynamoPairAssignmentDocumentRepository(user.email, clock), stubTribeId(), user)
    }

    @Test
    fun getPairAssignmentDocumentRecordsWillShowAllRecordsIncludingDeletions() = testAsync {
        val clock = MagicClock()
        val user = stubUser()
        val repository = DynamoPairAssignmentDocumentRepository(user.email, clock)
        setupAsync(object {
            val tribeId = stubTribeId()
            val pairAssignmentDocument = stubPairAssignmentDoc()
            val initialSaveTime = DateTime.now().minus(3.days)
            val updatedPairAssignmentDocument = pairAssignmentDocument.copy(
                pairs = listOf(pairOf(stubPlayer()).withPins(emptyList()))
            )
            val updatedSaveTime = initialSaveTime.plus(2.hours)
            val updatedSaveTime2 = initialSaveTime.plus(4.hours)
        }) {
            clock.currentTime = initialSaveTime
            repository.save(tribeId.with(pairAssignmentDocument))
            clock.currentTime = updatedSaveTime
            repository.save(tribeId.with(updatedPairAssignmentDocument))
            clock.currentTime = updatedSaveTime2
            repository.delete(tribeId, pairAssignmentDocument.id!!)
        } exerciseAsync {
            repository.getRecords(tribeId)
        } verifyAsync { result ->
            result
                .assertContains(Record(tribeId.with(pairAssignmentDocument), user.email, false, initialSaveTime))
                .assertContains(Record(tribeId.with(updatedPairAssignmentDocument), user.email, false, updatedSaveTime))
                .assertContains(Record(tribeId.with(updatedPairAssignmentDocument), user.email, true, updatedSaveTime2))
        }
    }

    @Test
    fun canSaveRawRecord() = testAsync {
        val clock = MagicClock()
        val user = stubUser()
        val repository = DynamoPairAssignmentDocumentRepository(user.email, clock)

        setupAsync(object {
            val tribeId = stubTribeId()
            val records = listOf(
                tribeRecord(tribeId, stubPairAssignmentDoc(), uuidString(), false, DateTime.now().minus(3.months)),
                tribeRecord(tribeId, stubPairAssignmentDoc(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }) exerciseAsync {
            records.forEach { repository.saveRawRecord(it) }
        } verifyAsync {
            with(repository.getRecords(tribeId)) {
                records.forEach { assertContains(it) }
            }
        }
    }


}