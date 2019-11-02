package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.model.tribe.TribeRepository

interface TribeListSyntax {
    val tribeRepository: TribeRepository
    fun getTribesAsync() = tribeRepository.getTribesAsync()
}