package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.data
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

interface PairAssignmentDocumentRepositoryValidator<R : PairAssignmentDocumentRepository> : RepositoryValidator<R, PartyContext<R>> {

    fun now() = Clock.System.now()

    @Test
    fun saveMultipleThenGetListWillReturnSavedDocumentsNewestToOldest() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val oldest = stubPairAssignmentDoc().copy(date = now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = now())
            val newest = stubPairAssignmentDoc().copy(date = now().plus(2.days))
        }.bind(),
    ) {
        partyId.with(listOf(middle, oldest, newest))
            .forEach { repository.save(it) }
    } exercise {
        repository.loadPairAssignments(partyId)
    } verifyWithWait { result ->
        result.data().map { it.document }
            .assertIsEqualTo(listOf(newest, middle, oldest))
    }

    @Test
    fun getCurrentPairAssignmentsOnlyReturnsTheNewest() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val oldest = stubPairAssignmentDoc().copy(date = now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = now())
            val newest = stubPairAssignmentDoc().copy(date = now().plus(2.days))
        }.bind(),
    ) {
        partyId.with(listOf(middle, oldest, newest))
            .forEach { repository.save(it) }
    } exercise {
        repository.getCurrentPairAssignments(partyId)
    } verify { result: PartyRecord<PairAssignmentDocument>? ->
        result?.element.assertIsEqualTo(newest)
    }

    @Test
    fun whenNoHistoryGetWillReturnEmptyList() = repositorySetup() exercise {
        repository.loadPairAssignments(partyId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun savedWillIncludeModificationDateAndUsername() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val pairAssignmentDoc = stubPairAssignmentDoc()
        }.bind(),
    ) {
        clock.currentTime = now().plus(4.hours)
        repository.save(partyId.with(pairAssignmentDoc))
    } exercise {
        repository.loadPairAssignments(partyId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsEqualTo(clock.currentTime)
            modifyingUserId.assertIsEqualTo(user.email)
        }
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val document = stubPairAssignmentDoc()
        }.bind(),
    ) {
        repository.save(partyId.with(document))
    } exercise {
        repository.deleteIt(partyId, document.id)
    } verifyWithWait { result ->
        result.assertIsEqualTo(true)
        repository.loadPairAssignments(partyId)
            .data().map { it.document }
            .assertIsEqualTo(emptyList())
    }

    @Test
    fun deleteWhenDocumentDoesNotExistWillReturnFalse() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val id = PairAssignmentDocumentId("${uuid4()}")
        }.bind(),
    ) exercise {
        repository.deleteIt(partyId, id)
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    @Test
    fun afterSavingUpdatedDocumentGetWillOnlyReturnTheUpdatedDocument() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val originalDateTime = now()
            val pairAssignmentDocument = stubPairAssignmentDoc().copy(date = originalDateTime)
            val updatedDateTime = originalDateTime.plus(3.days)
            val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
        }.bind(),
    ) {
        repository.save(partyId.with(pairAssignmentDocument))
    } exercise {
        repository.save(partyId.with(updatedDocument))
    } verifyWithWait {
        repository.loadPairAssignments(partyId).data().map { it.document }
            .assertIsEqualTo(listOf(updatedDocument))
    }
}
