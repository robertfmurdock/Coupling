package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.tribe.TribeRepository

class MemoryTribeRepository(
    override val userId: String,
    override val clock: TimeProvider,
    private val recordBackend: RecordBackend<Party> = SimpleRecordBackend()
) : TribeRepository, TypeRecordSyntax<Party>, RecordBackend<Party> by recordBackend {

    override suspend fun save(party: Party) = party.record().save()

    override suspend fun getTribeRecord(tribeId: PartyId) = tribeId.findParty()
        ?.takeUnless { it.isDeleted }

    override suspend fun getTribes() = recordList()
        .filterNot { it.isDeleted }

    private fun recordList() = records.groupBy { (tribe) -> tribe.id }
        .map { it.value.last() }

    override suspend fun delete(tribeId: PartyId) = tribeId.findParty()?.data.deleteRecord()

    private fun Party?.deleteRecord() = if (this == null) {
        false
    } else {
        deletionRecord().save()
        true
    }

    private fun PartyId.findParty() = recordList()
        .firstOrNull { it.data.id == this }

}
