package com.zegreatrob.coupling.repositoryvalidation

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.pairassignmentdocument.with
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import stubPairAssignmentDoc
import kotlin.test.Test

interface PairAssignmentDocumentRepositoryValidator {
    suspend fun withRepository(handler: suspend (PairAssignmentDocumentRepository, TribeId) -> Unit)

    private fun testRepository(block: suspend CoroutineScope.(PairAssignmentDocumentRepository, TribeId) -> Any?) =
        testAsync {
            withRepository { repository, tribeId -> block(repository, tribeId) }
        }

    @Test
    fun saveMultipleInTribeThenGetListWillReturnSavedDocumentsNewestToOldest() = testRepository { repository, tribeId ->
        setupAsync(object {
            val oldest = stubPairAssignmentDoc().copy(date = DateTime.now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = DateTime.now())
            val newest = stubPairAssignmentDoc().copy(date = DateTime.now().plus(2.days))
        }) {
            listOf(middle, oldest, newest)
                .forEach { repository.save(it.with(tribeId)) }
        } exerciseAsync {
            repository.getPairAssignments(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(newest, middle, oldest))
        }
    }

    @Test
    fun whenNoHistoryGetWillReturnEmptyList() = testRepository { repository, tribeId ->
        setupAsync(object {
        }) exerciseAsync {
            repository.getPairAssignments(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

}