package com.zegreatrob.coupling.repository.dynamo.pairs

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.repository.dynamo.DynamoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.dynamo.RepositoryContext
import com.zegreatrob.coupling.repository.dynamo.RepositoryContext.Companion.buildRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PartyContextData
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotools.types.collection.notEmptyListOf
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit
import kotlin.time.toDuration

val Int.years: Duration get() = (this * 365).toDuration(DurationUnit.DAYS)
val Int.months: Duration get() = (this * 30).toDuration(DurationUnit.DAYS)

@Suppress("unused")
class DynamoPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator<DynamoPairAssignmentDocumentRepository> {

    override val repositorySetup =
        asyncTestTemplate<PartyContext<DynamoPairAssignmentDocumentRepository>>(sharedSetup = {
            val clock = MagicClock()
            val user = stubUserDetails()
            PartyContextData(DynamoPairAssignmentDocumentRepository(user.id, clock), stubPartyId(), clock, user)
        })

    @Test
    fun getPairAssignmentDocumentRecordsWillShowAllRecordsIncludingDeletions() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context {
                val partyId = stubPartyId()
                val pairAssignmentDocument = stubPairAssignmentDoc()
                val initialSaveTime = now().minus(3.days)
                val updatedPairAssignmentDocument = pairAssignmentDocument.copy(
                    pairs = notEmptyListOf(pairOf(stubPlayer()).withPins(emptySet())),
                )
                val updatedSaveTime = initialSaveTime.plus(2.hours)
                val updatedSaveTime2 = initialSaveTime.plus(4.hours)
            }
        },
    ) exercise {
        clock.currentTime = initialSaveTime
        repository.save(partyId.with(pairAssignmentDocument))
        clock.currentTime = updatedSaveTime
        repository.save(partyId.with(updatedPairAssignmentDocument))
        clock.currentTime = updatedSaveTime2
        repository.deleteIt(partyId, pairAssignmentDocument.id)
    } verifyWithWait {
        repository.getRecords(partyId)
            .assertContains(Record(partyId.with(pairAssignmentDocument), user.id.value, false, initialSaveTime))
            .assertContains(Record(partyId.with(updatedPairAssignmentDocument), user.id.value, false, updatedSaveTime))
            .assertContains(Record(partyId.with(updatedPairAssignmentDocument), user.id.value, true, updatedSaveTime2))
    }

    @Test
    fun canSaveRawRecord() = asyncSetup.with(
        buildRepository { context ->
            object : Context by context {
                val partyId = stubPartyId()
                val records = listOf(
                    partyRecord(
                        partyId,
                        stubPairAssignmentDoc(),
                        uuidString().toNotBlankString().getOrThrow(),
                        false,
                        now().minus(3.months),
                    ),
                    partyRecord(
                        partyId,
                        stubPairAssignmentDoc(),
                        uuidString().toNotBlankString().getOrThrow(),
                        true,
                        now().minus(2.years),
                    ),
                )
            }
        },
    ) exercise {
        records.forEach { repository.saveRawRecord(it) }
    } verify {
        with(repository.getRecords(partyId)) {
            records.forEach { assertContains(it) }
        }
    }

    private fun <T> buildRepository(setupContext: (Context) -> T): suspend (Unit) -> T = {
        buildRepository(setupContext) { user, clock -> DynamoPairAssignmentDocumentRepository(user.id, clock) }.invoke()
    }
}

private typealias Context = RepositoryContext<DynamoPairAssignmentDocumentRepository>
