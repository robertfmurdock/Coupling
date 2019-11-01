package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.Deferred

interface PairAssignmentDocumentRepository : PairAssignmentDocumentSaver, PairAssignmentDocumentGetter,
    PairAssignmentDocumentDeleter

interface PairAssignmentDocumentDeleter {
    suspend fun delete(pairAssignmentDocumentId: PairAssignmentDocumentId) : Boolean
}

interface PairAssignmentDocumentGetter {
    fun getPairAssignmentsAsync(tribeId: TribeId): Deferred<List<PairAssignmentDocument>>
}

interface PairAssignmentDocumentSaver {
    suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument)
}