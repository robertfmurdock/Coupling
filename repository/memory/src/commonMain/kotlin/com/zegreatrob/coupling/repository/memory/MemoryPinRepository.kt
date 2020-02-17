package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository

class MemoryPinRepository(override val userEmail: String, override val clock: TimeProvider) : PinRepository,
    TypeRecordSyntax<TribeIdPin>,
    RecordSaveSyntax<TribeIdPin> {

    override var records = emptyList<Record<TribeIdPin>>()

    override suspend fun save(tribeIdPin: TribeIdPin) = tribeIdPin.record().save()

    override suspend fun getPins(tribeId: TribeId) = tribeId.recordList()
        .filterNot { it.isDeleted }

    private fun TribeId.recordList() = records.asSequence()
        .filter { (data) -> data.tribeId == this }
        .groupBy { (data) -> data.pin._id }
        .map { it.value.last() }

    override suspend fun deletePin(tribeId: TribeId, pinId: String) = recordWithId(tribeId, pinId)?.data
        .deleteRecord()

    private fun TribeIdPin?.deleteRecord() = if (this == null) {
        false
    } else {
        deletionRecord().save()
        true
    }

    private fun recordWithId(tribeId: TribeId, pinId: String) = tribeId.recordList()
        .find { (data) -> data.pin._id == pinId }

}
