package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.model.tribe.TribeRepository

interface TribeListSyntax {
    val tribeRepository: TribeRepository
    fun getTribesAsync() = tribeRepository.getTribesAsync()
}