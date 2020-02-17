package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeRepository

class MemoryTribeRepository : TribeRepository {

    private var tribeMap = emptyMap<Tribe, DateTime>()

    override suspend fun save(tribe: Tribe) = (tribe to DateTime.now()).let(::addToMap)

    private fun addToMap(pair: Pair<Tribe, DateTime>) {
        tribeMap = tribeMap + pair
    }

    override suspend fun getTribe(tribeId: TribeId): Tribe? = tribeMap.asSequence()
        .filter { it.key.id == tribeId }
        .lastOrNull()
        ?.key

    override suspend fun getTribes() = tribeMap.entries.groupBy { (tribe) -> tribe.id }
        .map { it.value.last().key }

    override suspend fun delete(tribeId: TribeId): Boolean {
        tribeMap = tribeMap.filterKeys { it.id != tribeId }
        return true
    }

}
