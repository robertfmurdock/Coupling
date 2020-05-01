package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.zegreatrob.coupling.model.data
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.async.waitForTest
import kotlin.test.Test
import kotlin.test.assertNotNull

interface PairAssignmentDocumentRepositoryValidator {
    suspend fun withRepository(
        clock: TimeProvider,
        handler: suspend (PairAssignmentDocumentRepository, TribeId, User) -> Unit
    )

    fun testRepository(block: (PairAssignmentDocumentRepository, TribeId, User, MagicClock) -> dynamic) =
        testAsync {
            val clock = MagicClock()

            withRepository(clock) { repository, tribeId, user ->
                waitForTest {
                    block(
                        repository,
                        tribeId,
                        user,
                        clock
                    )
                }
            }
        }

    @Test
    fun saveMultipleThenGetListWillReturnSavedDocumentsNewestToOldest() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val oldest = stubPairAssignmentDoc().copy(date = DateTime.now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = DateTime.now())
            val newest = stubPairAssignmentDoc().copy(date = DateTime.now().plus(2.days))
        }) {
            tribeId.with(listOf(middle, oldest, newest))
                .forEach { repository.save(it) }
        } exercise {
            repository.getPairAssignments(tribeId)
        } verify { result ->
            result.data().map { it.document }
                .assertIsEqualTo(listOf(newest, middle, oldest))
        }
    }

    @Test
    fun whenNoHistoryGetWillReturnEmptyList() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
        }) exercise {
            repository.getPairAssignments(tribeId)
        } verify { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun saveWillAssignIdWhenDocumentHasNone() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val pairAssignmentDoc = stubPairAssignmentDoc().copy(id = null)
        }) exercise {
            repository.save(tribeId.with(pairAssignmentDoc))
            repository.getPairAssignments(tribeId)
        } verify { result ->
            val resultDocument = result.data().map { it.document }.getOrNull(0)
            val resultId = resultDocument?.id
            assertNotNull(resultId)
            resultDocument.assertIsEqualTo(pairAssignmentDoc.copy(id = resultId))
        }
    }

    @Test
    fun savedWillIncludeModificationDateAndUsername() = testRepository { repository, tribeId, user, clock ->
        asyncSetup(object {
            val pairAssignmentDoc = stubPairAssignmentDoc()
        }) {
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
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val document = stubPairAssignmentDoc()
            val id = document.id!!
        }) {
            repository.save(tribeId.with(document))
        } exercise {
            repository.delete(tribeId, id)
        } verify { result ->
            result.assertIsEqualTo(true)
            repository.getPairAssignments(tribeId)
                .data().map { it.document }
                .assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun deleteWhenDocumentDoesNotExistWillReturnFalse() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val id = PairAssignmentDocumentId("${uuid4()}")
        }) exercise {
            repository.delete(tribeId, id)
        } verify { result ->
            result.assertIsEqualTo(false)
        }
    }

    @Test
    fun afterSavingUpdatedDocumentGetWillOnlyReturnTheUpdatedDocument() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val originalDateTime = DateTime.now()
            val pairAssignmentDocument = stubPairAssignmentDoc().copy(date = originalDateTime)
            val updatedDateTime = originalDateTime.plus(3.days)
            val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
        }) {
            repository.save(tribeId.with(pairAssignmentDocument))
        } exercise {
            repository.save(tribeId.with(updatedDocument))
            repository.getPairAssignments(tribeId)
        } verify { result ->
            result.data().map { it.document }
                .assertIsEqualTo(listOf(updatedDocument))
        }
    }

}