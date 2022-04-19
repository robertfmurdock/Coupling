package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.partyId
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.repository.pin.PinRepository

class MemoryPinRepository(
    override val userId: String,
    override val clock: TimeProvider,
    private val recordBackend: RecordBackend<PartyElement<Pin>> = SimpleRecordBackend()
) : PinRepository,
    TypeRecordSyntax<PartyElement<Pin>>,
    RecordBackend<PartyElement<Pin>> by recordBackend {

    override suspend fun save(partyPin: PartyElement<Pin>) =
        partyPin.copy(element = with(partyPin.element) { copy(id = id ?: "${com.benasher44.uuid.uuid4()}") })
            .record().save()

    override suspend fun getPins(partyId: PartyId) = partyId.recordList()
        .filterNot { it.isDeleted }

    private fun PartyId.recordList() = records.asSequence()
        .filter { (data) -> data.partyId == this }
        .groupBy { (data) -> data.pin.id }
        .map { it.value.last() }

    override suspend fun deletePin(partyId: PartyId, pinId: String) = recordWithId(partyId, pinId)?.data
        .deleteRecord()

    private fun PartyElement<Pin>?.deleteRecord() =
        if (this == null) {
            false
        } else {
            deletionRecord().save()
            true
        }

    private fun recordWithId(tribeId: PartyId, pinId: String) = tribeId.recordList()
        .find { (data) -> data.pin.id == pinId }
}
