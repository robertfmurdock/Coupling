package com.zegreatrob.coupling.model.tribe

interface TribeIdDeleteSyntax {

    val tribeRepository: TribeDelete

    suspend fun TribeId.delete() = tribeRepository.delete(this)
}