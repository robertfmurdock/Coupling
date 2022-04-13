package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.pairassignmentdocument.partyId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository

class MemoryPairAssignmentDocumentRepository(
    override val userId: String,
    override val clock: TimeProvider,
    private val recordBackend: RecordBackend<PartyElement<PairAssignmentDocument>> = SimpleRecordBackend()
) :
    PairAssignmentDocumentRepository,
    TypeRecordSyntax<PartyElement<PairAssignmentDocument>>,
    RecordBackend<PartyElement<PairAssignmentDocument>> by recordBackend {

    override suspend fun save(tribeIdPairAssignmentDocument: PartyElement<PairAssignmentDocument>) =
        tribeIdPairAssignmentDocument
            .record()
            .save()

    override suspend fun getPairAssignments(tribeId: PartyId): List<Record<PartyElement<PairAssignmentDocument>>> =
        tribeId.records()
            .filterNot { it.isDeleted }
            .sortedByDescending { it.data.document.date }

    override suspend fun getCurrentPairAssignments(tribeId: PartyId) = tribeId.records()
        .filterNot { it.isDeleted }
        .maxByOrNull { it.data.document.date }

    private fun PartyId.records() = records.asSequence()
        .filter { (data) -> data.partyId == this }
        .groupBy { (data) -> data.document.id }
        .map { it.value.last() }

    override suspend fun delete(tribeId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId): Boolean {
        val tribeIdPairAssignmentDocument = record(tribeId, pairAssignmentDocumentId)?.data

        return if (tribeIdPairAssignmentDocument == null) {
            false
        } else {
            tribeIdPairAssignmentDocument.deletionRecord().save()
            true
        }
    }

    private fun record(tribeId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId) = tribeId.records()
        .find { (data) -> data.document.id == pairAssignmentDocumentId }

}
