package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.ExtendedBoostRepository
import kotlinx.datetime.Clock

class MemoryBoostRepository(
    override val userId: String,
    override val clock: Clock,
    private val recordBackend: RecordBackend<Boost> = SimpleRecordBackend(),
) : TypeRecordSyntax<Boost>,
    RecordBackend<Boost> by recordBackend,
    ExtendedBoostRepository {

    override suspend fun get(): Record<Boost>? = records.lastOrNull { it.data.userId == userId }
        ?.takeUnless { it.isDeleted }

    override suspend fun save(boost: Boost) = boost.record().save()

    override suspend fun deleteIt() {
        get()?.data?.deletionRecord()?.save()
    }

    override suspend fun getByPartyId(partyId: PartyId): Record<Boost>? = allLatestRecords().firstOrNull {
        it.data.partyIds.contains(partyId)
    }

    private fun allLatestRecords() = records.groupBy { it.data.userId }.map { it.value.last() }
}
