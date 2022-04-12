package com.zegreatrob.coupling.repository.dynamo.pairs

import com.soywiz.klock.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.repository.dynamo.DynamoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.dynamo.RepositoryContext
import com.zegreatrob.coupling.repository.dynamo.RepositoryContext.Companion.buildRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.repository.validation.TribeContextData
import com.zegreatrob.coupling.stubmodel.*
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Suppress("unused")
class DynamoPairAssignmentDocumentRepositoryTest :
    PairAssignmentDocumentRepositoryValidator<DynamoPairAssignmentDocumentRepository> {

    override val repositorySetup =
        asyncTestTemplate<TribeContext<DynamoPairAssignmentDocumentRepository>>(sharedSetup = {
            val clock = MagicClock()
            val user = stubUser()
            TribeContextData(DynamoPairAssignmentDocumentRepository(user.email, clock), stubPartyId(), clock, user)
        })

    @Test
    fun getPairAssignmentDocumentRecordsWillShowAllRecordsIncludingDeletions() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context {
                val tribeId = stubPartyId()
                val pairAssignmentDocument = stubPairAssignmentDoc()
                val initialSaveTime = DateTime.now().minus(3.days)
                val updatedPairAssignmentDocument = pairAssignmentDocument.copy(
                    pairs = listOf(pairOf(stubPlayer()).withPins(emptyList()))
                )
                val updatedSaveTime = initialSaveTime.plus(2.hours)
                val updatedSaveTime2 = initialSaveTime.plus(4.hours)
            }
        }) exercise {
        clock.currentTime = initialSaveTime
        repository.save(tribeId.with(pairAssignmentDocument))
        clock.currentTime = updatedSaveTime
        repository.save(tribeId.with(updatedPairAssignmentDocument))
        clock.currentTime = updatedSaveTime2
        repository.delete(tribeId, pairAssignmentDocument.id)
    } verify {
        repository.getRecords(tribeId)
            .assertContains(Record(tribeId.with(pairAssignmentDocument), user.email, false, initialSaveTime))
            .assertContains(Record(tribeId.with(updatedPairAssignmentDocument), user.email, false, updatedSaveTime))
            .assertContains(Record(tribeId.with(updatedPairAssignmentDocument), user.email, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup.with(buildRepository { context ->
        object : Context by context {
            val tribeId = stubPartyId()
            val records = listOf(
                tribeRecord(tribeId, stubPairAssignmentDoc(), uuidString(), false, DateTime.now().minus(3.months)),
                tribeRecord(tribeId, stubPairAssignmentDoc(), uuidString(), true, DateTime.now().minus(2.years))
            )
        }
    }) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verify {
        with(repository.getRecords(tribeId)) {
            records.forEach { assertContains(it) }
        }
    }

    private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
        buildRepository(setupContext) { user, clock ->
            DynamoPairAssignmentDocumentRepository(user.email, clock)
        }.invoke()
    }

}

private typealias Context = RepositoryContext<DynamoPairAssignmentDocumentRepository>
