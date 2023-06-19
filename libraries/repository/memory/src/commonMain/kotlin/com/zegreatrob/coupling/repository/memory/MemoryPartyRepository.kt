package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyRepository
import korlibs.time.TimeProvider

class MemoryPartyRepository(
    override val userId: String,
    override val clock: TimeProvider = TimeProvider,
    private val recordBackend: RecordBackend<Party> = SimpleRecordBackend(),
) : PartyRepository, TypeRecordSyntax<Party>, RecordBackend<Party> by recordBackend {

    override suspend fun save(party: Party) = party.record().save()

    override suspend fun getPartyRecord(partyId: PartyId) = partyId.findParty()
        ?.takeUnless { it.isDeleted }

    override suspend fun loadParties() = recordList()
        .filterNot { it.isDeleted }

    private fun recordList() = records.groupBy { (party) -> party.id }
        .map { it.value.last() }

    override suspend fun deleteIt(partyId: PartyId) = partyId.findParty()?.data.deleteRecord()

    private fun Party?.deleteRecord() = if (this == null) {
        false
    } else {
        deletionRecord().save()
        true
    }

    private fun PartyId.findParty() = recordList()
        .firstOrNull { it.data.id == this }
}
