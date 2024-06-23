package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import kotlinx.datetime.Clock

class MemoryPairAssignmentDocumentRepository(
    override val userId: String,
    override val clock: Clock,
    private val recordBackend: RecordBackend<PartyElement<PairAssignmentDocument>> = SimpleRecordBackend(),
) : PairAssignmentDocumentRepository,
    TypeRecordSyntax<PartyElement<PairAssignmentDocument>>,
    RecordBackend<PartyElement<PairAssignmentDocument>> by recordBackend {

    override suspend fun save(partyPairDocument: PartyElement<PairAssignmentDocument>) =
        partyPairDocument
            .record()
            .save()

    override suspend fun loadPairAssignments(partyId: PartyId): List<Record<PartyElement<PairAssignmentDocument>>> =
        partyId.records()
            .filterNot { it.isDeleted }
            .sortedByDescending { it.data.document.date }

    override suspend fun getCurrentPairAssignments(partyId: PartyId) = partyId.records()
        .filterNot { it.isDeleted }
        .maxByOrNull { it.data.document.date }

    private fun PartyId.records() = records.asSequence()
        .filter { (data) -> data.partyId == this }
        .groupBy { (data) -> data.document.id }
        .map { it.value.last() }

    override suspend fun deleteIt(partyId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId): Boolean {
        val partyIdPairAssignmentDocument = record(partyId, pairAssignmentDocumentId)?.data

        return if (partyIdPairAssignmentDocument == null) {
            false
        } else {
            partyIdPairAssignmentDocument.deletionRecord().save()
            true
        }
    }

    private fun record(partyId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId) = partyId.records()
        .find { (data) -> data.document.id == pairAssignmentDocumentId }
}
