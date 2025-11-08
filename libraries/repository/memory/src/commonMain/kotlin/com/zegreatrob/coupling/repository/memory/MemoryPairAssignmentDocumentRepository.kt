package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import kotlin.time.Clock

class MemoryPairAssignmentDocumentRepository(
    override val userId: UserId,
    override val clock: Clock,
    private val recordBackend: RecordBackend<PartyElement<PairingSet>> = SimpleRecordBackend(),
) : PairAssignmentDocumentRepository,
    TypeRecordSyntax<PartyElement<PairingSet>>,
    RecordBackend<PartyElement<PairingSet>> by recordBackend {

    override suspend fun save(partyPairDocument: PartyElement<PairingSet>) = partyPairDocument
        .record()
        .save()

    override suspend fun loadPairAssignments(partyId: PartyId): List<Record<PartyElement<PairingSet>>> = partyId.records()
        .filterNot { it.isDeleted }
        .sortedByDescending { it.data.document.date }

    override suspend fun getCurrentPairAssignments(partyId: PartyId) = partyId.records()
        .filterNot { it.isDeleted }
        .maxByOrNull { it.data.document.date }

    private fun PartyId.records() = records.asSequence()
        .filter { (data) -> data.partyId == this }
        .groupBy { (data) -> data.document.id }
        .map { it.value.last() }

    override suspend fun deleteIt(partyId: PartyId, pairingSetId: PairingSetId): Boolean {
        val partyIdPairAssignmentDocument = record(partyId, pairingSetId)?.data

        return if (partyIdPairAssignmentDocument == null) {
            false
        } else {
            partyIdPairAssignmentDocument.deletionRecord().save()
            true
        }
    }

    private fun record(partyId: PartyId, pairingSetId: PairingSetId) = partyId.records()
        .find { (data) -> data.document.id == pairingSetId }
}
