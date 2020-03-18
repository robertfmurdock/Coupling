package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.MemoryPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubPairAssignmentDoc
import stubTribeId
import stubUser
import kotlin.test.Test

class CompoundPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator {
    override suspend fun withRepository(
        clock: TimeProvider,
        handler: suspend (PairAssignmentDocumentRepository, TribeId, User) -> Unit
    ) {
        val user = stubUser()

        val repository1 = MemoryPairAssignmentDocumentRepository(user.email, clock)
        val repository2 = MemoryPairAssignmentDocumentRepository(user.email, clock)

        val compoundRepo = CompoundPairAssignmentDocumentRepository(repository1, repository2)

        handler(compoundRepo, stubTribeId(), user)
    }

    @Test
    fun saveWillWriteToSecondRepository() = testAsync {
        setupAsync(object {
            val stubUser = stubUser()

            val repository1 = MemoryPairAssignmentDocumentRepository(stubUser.email, TimeProvider)
            val repository2 = MemoryPairAssignmentDocumentRepository(stubUser.email, TimeProvider)

            val compoundRepo = CompoundPairAssignmentDocumentRepository(repository1, repository2)

            val tribeId = stubTribeId()
            val pairAssignmentDocument = stubPairAssignmentDoc()
        }) exerciseAsync {
            compoundRepo.save(tribeId.with(pairAssignmentDocument))
        } verifyAsync {
            repository2.getPairAssignments(tribeId).map { it.data.document }
                .find { it.id == pairAssignmentDocument.id }
                .assertIsEqualTo(pairAssignmentDocument)
        }
    }

    @Test
    fun deleteWillWriteToSecondRepository() = testAsync {
        setupAsync(object {
            val stubUser = stubUser()

            val repository1 = MemoryPairAssignmentDocumentRepository(stubUser.email, TimeProvider)
            val repository2 = MemoryPairAssignmentDocumentRepository(stubUser.email, TimeProvider)

            val compoundRepo = CompoundPairAssignmentDocumentRepository(repository1, repository2)

            val tribeId = stubTribeId()
            val pairAssignmentDocument = stubPairAssignmentDoc()
        }) exerciseAsync {
            compoundRepo.save(tribeId.with(pairAssignmentDocument))
            compoundRepo.delete(tribeId, pairAssignmentDocument.id!!)
        } verifyAsync {
            repository2.getPairAssignments(tribeId).map { it.data.document }
                .find { it.id == pairAssignmentDocument.id }
                .assertIsEqualTo(null)
        }
    }
}