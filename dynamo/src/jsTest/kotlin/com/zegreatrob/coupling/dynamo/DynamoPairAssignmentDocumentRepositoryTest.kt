package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repositoryvalidation.PairAssignmentDocumentRepositoryValidator
import stubTribeId

@Suppress("unused")
class DynamoPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator {
    override suspend fun withRepository(handler: suspend (PairAssignmentDocumentRepository, TribeId) -> Unit) {
        handler(DynamoPairAssignmentDocumentRepository(), stubTribeId())
    }

    override fun saveAndDeleteThenGetWillReturnNothing(): Any? {
        TODO()
    }

    override fun deleteWhenDocumentDoesNotExistWillReturnFalse(): Any? {
        TODO()
    }

}