package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.zegreatrob.coupling.model.data
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test
import kotlin.test.assertNotNull

interface PairAssignmentDocumentRepositoryValidator<R : PairAssignmentDocumentRepository> :
    RepositoryValidator<R, TribeContext<R>> {

    @Test
    fun saveMultipleThenGetListWillReturnSavedDocumentsNewestToOldest() =
        repositorySetup(object : TribeContextMint<R>() {
            val oldest = stubPairAssignmentDoc().copy(date = DateTime.now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = DateTime.now())
            val newest = stubPairAssignmentDoc().copy(date = DateTime.now().plus(2.days))
        }.bind()) {
            tribeId.with(listOf(middle, oldest, newest))
                .forEach { repository.save(it) }
        } exercise {
            repository.getPairAssignments(tribeId)
        } verify { result ->
            result.data().map { it.document }
                .assertIsEqualTo(listOf(newest, middle, oldest))
        }

    @Test
    fun whenNoHistoryGetWillReturnEmptyList() = repositorySetup() exercise {
        repository.getPairAssignments(tribeId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun saveWillAssignIdWhenDocumentHasNone() = repositorySetup(object : TribeContextMint<R>() {
        val pairAssignmentDoc = stubPairAssignmentDoc().copy(id = null)
    }.bind()) exercise {
        repository.save(tribeId.with(pairAssignmentDoc))
        repository.getPairAssignments(tribeId)
    } verify { result ->
        val resultDocument = result.data().map { it.document }.getOrNull(0)
        val resultId = resultDocument?.id
        assertNotNull(resultId)
        resultDocument.assertIsEqualTo(pairAssignmentDoc.copy(id = resultId))
    }

    @Test
    fun savedWillIncludeModificationDateAndUsername() = repositorySetup(object : TribeContextMint<R>() {
        val pairAssignmentDoc = stubPairAssignmentDoc()
    }.bind()) {
        clock.currentTime = DateTime.now().plus(4.hours)
        repository.save(tribeId.with(pairAssignmentDoc))
    } exercise {
        repository.getPairAssignments(tribeId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsEqualTo(clock.currentTime)
            modifyingUserId.assertIsEqualTo(user.email)
        }
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = repositorySetup(object : TribeContextMint<R>() {
        val document = stubPairAssignmentDoc()
        val id = document.id!!
    }.bind()) {
        repository.save(tribeId.with(document))
    } exercise {
        repository.delete(tribeId, id)
    } verify { result ->
        result.assertIsEqualTo(true)
        repository.getPairAssignments(tribeId)
            .data().map { it.document }
            .assertIsEqualTo(emptyList())
    }

    @Test
    fun deleteWhenDocumentDoesNotExistWillReturnFalse() = repositorySetup(object : TribeContextMint<R>() {
        val id = PairAssignmentDocumentId("${uuid4()}")
    }.bind()) exercise {
        repository.delete(tribeId, id)
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    @Test
    fun afterSavingUpdatedDocumentGetWillOnlyReturnTheUpdatedDocument() =
        repositorySetup(object : TribeContextMint<R>() {
            val originalDateTime = DateTime.now()
            val pairAssignmentDocument = stubPairAssignmentDoc().copy(date = originalDateTime)
            val updatedDateTime = originalDateTime.plus(3.days)
            val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
        }.bind()) {
            repository.save(tribeId.with(pairAssignmentDocument))
        } exercise {
            repository.save(tribeId.with(updatedDocument))
            repository.getPairAssignments(tribeId)
        } verify { result ->
            result.data().map { it.document }
                .assertIsEqualTo(listOf(updatedDocument))
        }

}