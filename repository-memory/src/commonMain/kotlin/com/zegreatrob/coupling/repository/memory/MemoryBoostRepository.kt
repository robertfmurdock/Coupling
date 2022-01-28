package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.BoostRepository

class MemoryBoostRepository(
    override val userId: String,
    override val clock: TimeProvider,
    private val recordBackend: RecordBackend<Boost> = SimpleRecordBackend()
) : TypeRecordSyntax<Boost>, RecordBackend<Boost> by recordBackend, BoostRepository {

    override fun get(): Record<Boost>? = records.lastOrNull { it.data.userId == userId }

    override fun save(boost: Boost) = boost.record().save()

    override fun getByTribeId(tribeId: TribeId): Record<Boost>? = allLatestRecords().firstOrNull {
        it.data.tribeIds.contains(tribeId)
    }

    override fun allLatestRecords() = records.groupBy { it.data.id }.map { it.value.last() }

}
