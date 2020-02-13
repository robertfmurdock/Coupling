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

    override fun afterSavingAndUpdatedDocumentGetWillOnlyReturnTheUpdatedDocument(): Any? {
        TODO()
    }

    override fun deleteWhenDocumentDoesNotExistWillReturnFalse(): Any? {
        TODO()
    }

    override fun saveAndDeleteThenGetWillReturnNothing(): Any? {
        TODO()
    }

    override fun saveWillAssignIdWhenDocumentHasNone(): Any? {
        TODO()
    }

    override fun whenNoHistoryGetWillReturnEmptyList(): Any? {
        TODO()
    }

}