package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository

class MemoryPinRepository : PinRepository, TypeRecordSyntax<TribeIdPin> {

    var records = emptyList<Record<TribeIdPin>>()

    override suspend fun save(tribeIdPin: TribeIdPin) {
        tribeIdPin.record().save()
    }

    private fun Record<TribeIdPin>.save() {
        records = records + this
    }

    override suspend fun getPins(tribeId: TribeId) = tribeId.recordList()
        .filterNot { it.isDeleted }
        .map { it.data.pin }

    private fun TribeId.recordList() = records.asSequence()
        .filter { (data) -> data.tribeId == this }
        .groupBy { (data) -> data.pin._id }
        .map { it.value.last() }

    override suspend fun deletePin(tribeId: TribeId, pinId: String): Boolean {
        val tribeIdPin = recordWithId(tribeId, pinId)?.data
        return if (tribeIdPin == null) {
            false
        } else {
            tribeIdPin.deleteRecord().save()
            true
        }
    }

    private fun recordWithId(tribeId: TribeId, pinId: String) = tribeId.recordList()
        .find { (data) -> data.pin._id == pinId }

}
