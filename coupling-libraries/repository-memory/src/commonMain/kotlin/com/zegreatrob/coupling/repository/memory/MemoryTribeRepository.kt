package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeRepository

class MemoryTribeRepository(
    override val userId: String,
    override val clock: TimeProvider,
    private val recordBackend: RecordBackend<Tribe> = SimpleRecordBackend()
) : TribeRepository, TypeRecordSyntax<Tribe>, RecordBackend<Tribe> by recordBackend {

    override suspend fun save(tribe: Tribe) = tribe.record().save()

    override suspend fun getTribeRecord(tribeId: TribeId) = tribeId.findTribe()
        ?.takeUnless { it.isDeleted }

    override suspend fun getTribes() = recordList()
        .filterNot { it.isDeleted }

    private fun recordList() = records.groupBy { (tribe) -> tribe.id }
        .map { it.value.last() }

    override suspend fun delete(tribeId: TribeId) = tribeId.findTribe()?.data.deleteRecord()

    private fun Tribe?.deleteRecord() = if (this == null) {
        false
    } else {
        deletionRecord().save()
        true
    }

    private fun TribeId.findTribe() = recordList()
        .firstOrNull { it.data.id == this }

}
