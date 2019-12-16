package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdDeleteSyntax {

    val tribeRepository: TribeDelete

    suspend fun TribeId.delete() = tribeRepository.delete(this)
}