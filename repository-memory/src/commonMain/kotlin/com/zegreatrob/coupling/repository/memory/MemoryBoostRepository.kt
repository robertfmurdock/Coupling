package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId

class MemoryBoostRepository(
    override val userId: String,
    override val clock: TimeProvider,
    private val recordBackend: RecordBackend<Boost> = SimpleRecordBackend()
) : TypeRecordSyntax<Boost>, RecordBackend<Boost> by recordBackend {

    fun get(): Record<Boost>? = records.lastOrNull { it.data.userId == userId }

    fun save(boost: Boost) = boost.record().save()

    fun getByTribeId(tribeId: TribeId): Record<Boost>? = allLatestRecords().firstOrNull {
        it.data.tribeIds.contains(tribeId)
    }

    private fun allLatestRecords() = records.groupBy { it.data.id }.map { it.value.last() }

}
