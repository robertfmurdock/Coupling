package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeRepository

class CompoundTribeRepository(private val repository1: TribeRepository, private val repository2: TribeRepository) :
    TribeRepository by repository1 {

    override suspend fun save(tribe: Tribe) = arrayOf(repository1, repository2).forEach { it.save(tribe) }

    override suspend fun delete(tribeId: TribeId) = repository1.delete(tribeId)
        .also { repository2.delete(tribeId) }

}
