package com.zegreatrob.coupling.repository.memory

import com.benasher44.uuid.uuid4
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.pairassignmentdocument.tribeId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository

class MemoryPairAssignmentDocumentRepository(override val userEmail: String, override val clock: TimeProvider) :
    PairAssignmentDocumentRepository,
    TypeRecordSyntax<TribeIdPairAssignmentDocument>,
    RecordSaveSyntax<TribeIdPairAssignmentDocument> {

    override var records = emptyList<Record<TribeIdPairAssignmentDocument>>()

    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) =
        tribeIdPairAssignmentDocument.addMissingId()
            .record()
            .save()

    private fun TribeIdPairAssignmentDocument.addMissingId() = copy(
        element = document.copy(
            id = document.id ?: "${uuid4()}".let(::PairAssignmentDocumentId)
        )
    )

    override suspend fun getPairAssignments(tribeId: TribeId): List<Record<TribeIdPairAssignmentDocument>> =
        tribeId.records()
            .filterNot { it.isDeleted }
            .sortedByDescending { it.data.document.date }

    private fun TribeId.records() = records.asSequence()
        .filter { (data) -> data.tribeId == this }
        .groupBy { (data) -> data.document.id }
        .map { it.value.last() }

    override suspend fun delete(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId): Boolean {
        val tribeIdPairAssignmentDocument = record(tribeId, pairAssignmentDocumentId)?.data

        return if (tribeIdPairAssignmentDocument == null) {
            false
        } else {
            tribeIdPairAssignmentDocument.deletionRecord().save()
            true
        }
    }

    private fun record(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId) = tribeId.records()
        .find { (data) -> data.document.id == pairAssignmentDocumentId }

}
