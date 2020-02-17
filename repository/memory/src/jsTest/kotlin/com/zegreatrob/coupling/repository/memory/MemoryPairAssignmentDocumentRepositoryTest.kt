package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import stubTribeId
import stubUser

@Suppress("unused")
class MemoryPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator {
    override suspend fun withRepository(handler: suspend (PairAssignmentDocumentRepository, TribeId) -> Unit) {
        val user = stubUser()
        handler(MemoryPairAssignmentDocumentRepository(user.email), stubTribeId())
    }
}