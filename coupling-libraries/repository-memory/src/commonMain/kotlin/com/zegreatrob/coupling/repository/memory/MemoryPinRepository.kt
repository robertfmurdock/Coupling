package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.pin.tribeId
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.repository.pin.PinRepository

class MemoryPinRepository(
    override val userId: String,
    override val clock: TimeProvider,
    private val recordBackend: RecordBackend<TribeIdPin> = SimpleRecordBackend()
) : PinRepository,
    TypeRecordSyntax<TribeIdPin>,
    RecordBackend<TribeIdPin> by recordBackend {

    override suspend fun save(tribeIdPin: TribeIdPin) =
        tribeIdPin.copy(element = with(tribeIdPin.element) { copy(id = id ?: "${com.benasher44.uuid.uuid4()}") })
            .record().save()

    override suspend fun getPins(tribeId: PartyId) = tribeId.recordList()
        .filterNot { it.isDeleted }

    private fun PartyId.recordList() = records.asSequence()
        .filter { (data) -> data.tribeId == this }
        .groupBy { (data) -> data.pin.id }
        .map { it.value.last() }

    override suspend fun deletePin(tribeId: PartyId, pinId: String) = recordWithId(tribeId, pinId)?.data
        .deleteRecord()

    private fun TribeIdPin?.deleteRecord() = if (this == null) {
        false
    } else {
        deletionRecord().save()
        true
    }

    private fun recordWithId(tribeId: PartyId, pinId: String) = tribeId.recordList()
        .find { (data) -> data.pin.id == pinId }

}
